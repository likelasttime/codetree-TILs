import java.io.*;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Main {
    static Map<Integer, Node> trees;
    static List<Integer> rootLst;
    static final int COLOR_CNT = 6;

    static class Node {
        int mId;        // 고유 번호
        int pId;        // 부모 노드 번호
        int color;      // 빨간색:1, 주황색:2, 노랑색:3, 초록색:4, 파란색:5
        int maxDepth;   // 서브트리의 최대 깊이
        List<Integer> childs;

        Node(int mId, int pId, int color, int maxDepth) {
            this.mId = mId;
            this.pId = pId;
            this.color = color;
            this.maxDepth = maxDepth;
            this.childs = new ArrayList();
        }
    }

    static class NodeInfo {
        boolean[] visit;        // 찾은 색깔 체크 배열
        int cnt;                // 가치

        NodeInfo() {
            this.cnt = 0;
            this.visit = new boolean[COLOR_CNT];
        }

        /*
            색 추가
        */
        public void updateVisit(boolean[] visit) {
            for(int i=0; i<COLOR_CNT; i++) {
                if(this.visit[i]) {     
                    visit[i] = true;
                }
            }
        }

        /*
            가치의 제곱 계산
        */
        public void calValue() {
            int tmp = 0;
            for(int i=0; i<COLOR_CNT; i++) {
                if(this.visit[i]) {
                    tmp++;
                }
            }
            this.cnt = tmp * tmp;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st;
        int q = Integer.parseInt(br.readLine());      // 1 <= 명령의 수 <= 100,000
        trees = new HashMap();
        rootLst = new ArrayList();

        // 명령의 정보가 주어짐
        for(int i=0; i<q; i++) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());

            if(cmd == 100) {  // 노드 추가
                int mId = Integer.parseInt(st.nextToken());     // 1 <= 고유한 번호 <= 100,000
                int pId = Integer.parseInt(st.nextToken());     // 1 <= 부모 노드 번호 <= 100,000
                int color = Integer.parseInt(st.nextToken());   // 1 <= 색깔 <= 5
                int maxDepth = Integer.parseInt(st.nextToken());    // 1 <= 최대 깊이 <= 100

                if(pId == -1) {     // 루트 노드일 때
                    trees.put(mId, new Node(mId, pId, color, maxDepth));
                    rootLst.add(mId);
                } else if(isAbleToAdd(pId)) {       // 부모 노드 pId에 자식을 추가할 수 있으면
                    trees.put(mId, new Node(mId, pId, color, maxDepth));
                    trees.get(pId).childs.add(mId);     // 부모 노드에 자식 노드 추가
                }
            } else if(cmd == 200) {     // 색깔 변경
                int mId = Integer.parseInt(st.nextToken());     // 루트
                int color = Integer.parseInt(st.nextToken());   // 색깔

                changeColor(mId, color);
            } else if(cmd == 300) {     // 색깔 조회
                int mId = Integer.parseInt(st.nextToken());     // 노드 번호

                bw.write(trees.get(mId).color + "\n");
            }
            else {  // 점수 조회
                // 모든 노드의 가치를 계산하여, 가치 제곱의 합을 출력

                int total = 0;
                for(int id : rootLst) {
                    total += calDifferentColor(id).cnt;
                }

                bw.write(total + "\n");
            }
        }

        bw.flush();
    }

    /*
        부모 노드 pId에 노드를 더 추가할 수 있다면 true 반환
    */
    private static boolean isAbleToAdd(int pId) {
        Node parent = trees.get(pId);
        if(parent.childs.size() + 1 == parent.maxDepth) {      // 현재 자식을 최대로 가졌다면(자기 자신을 포함하니까 +1)
            return false;
        }
        return true;
    }

    /*
        서브트리의 모든 노드의 색깔을 지정된 color로 변경
    */
    private static void changeColor(int mId, int color) {
        Node node = trees.get(mId);
        node.color = color;

        for(int child : node.childs) {
            changeColor(child, color);
        }
    }

    /*
        모든 노드의 가치를 계산
        mId: 현재 노드 번호
    */
    private static NodeInfo calDifferentColor(int mId) {
        Node cur = trees.get(mId);
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.visit[cur.color] = true;   // 현재 노드의 색깔 체크
        int sum = 0;    // 현재 노드의 가치

        for(Integer child : cur.childs) {   // 자식 노드 탐색
            NodeInfo endNode = calDifferentColor(child);    // 끝까지 탐색한 후 노드 반환받기
            endNode.updateVisit(nodeInfo.visit);    // 그동안 찾은 색깔들 추가
            sum += endNode.cnt;     // 노드의 가치 더하기
        }

        nodeInfo.calValue();        // 가치 제곱의 합 계산
        nodeInfo.cnt += sum;        // 노드의 가치 합 추가
        return nodeInfo;    // 더이상 자식 노드가 없으면 반환
    }
}