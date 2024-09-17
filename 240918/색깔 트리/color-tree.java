import java.io.*;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

public class Main {
    static Node[] trees;
    static List<Integer> rootLst;       // 루트 노드를 저장하는 리스트
    static final int COLOR_CNT = 6;
    static final int MAX_NODE_CNT = 100005;

    static class Node {
        int mId;        // 고유 번호
        int pId;        // 부모 노드 번호
        int color;      // 빨간색:1, 주황색:2, 노랑색:3, 초록색:4, 파란색:5
        int maxDepth;   // 서브트리의 최대 깊이
        int lastUpdate; // 노드를 추가하거나 색깔을 변경한 최근 시점
        List<Integer> childs;   // 자식 노드 번호를 저장하는 리스트

        Node() {
            
        }

        Node(int mId, int pId, int color, int maxDepth, int lastUpdate) {
            this.mId = mId;
            this.pId = pId;
            this.color = color;
            this.maxDepth = maxDepth;
            this.childs = new ArrayList();
            this.lastUpdate = lastUpdate;
        }
    }

    static class NodeInfo {
        int[] cnt = new int[COLOR_CNT];

        /*
            컬러별 갯수를 갱신
         */
        public NodeInfo updateVisit(NodeInfo nodeInfo) {
            NodeInfo newNode = new NodeInfo();

            for(int i=1; i<COLOR_CNT; i++) {
                newNode.cnt[i] = this.cnt[i] + nodeInfo.cnt[i];
            }
            return newNode;
        }

        /*
            제곱 가치 구하기
         */
        public int calValue() {
            int tmp = 0;
            for(int i=1; i<COLOR_CNT; i++) {
                if(this.cnt[i] > 0) {
                    tmp++;
                }
            }
            return tmp * tmp;
        }

    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st;
        int q = Integer.parseInt(br.readLine());      // 1 <= 명령의 수 <= 100,000
        trees = new Node[MAX_NODE_CNT];
        rootLst = new ArrayList();

        for(int i=0; i<MAX_NODE_CNT; i++) {
            trees[i] = new Node();
        }

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
                    trees[mId] = new Node(mId, pId, color, maxDepth, i);
                    rootLst.add(mId);
                } else if(isAbleToAdd(pId)) {       // 부모 노드 pId에 자식을 추가할 수 있으면
                    trees[mId] = new Node(mId, pId, color, maxDepth, i);
                    trees[pId].childs.add(mId);
                }
            } else if(cmd == 200) {     // 색깔 변경
                int mId = Integer.parseInt(st.nextToken());     // 루트
                int color = Integer.parseInt(st.nextToken());   // 색깔

                changeColor(mId, color, i);
            } else if(cmd == 300) {     // 색깔 조회
                int mId = Integer.parseInt(st.nextToken());     // 노드 번호

                bw.write(getColor(mId)[0] + "\n");
            }
            else {  // 점수 조회
                // 모든 노드의 가치를 계산하여, 가치 제곱의 합을 출력
                int total = 0;
                for(int id : rootLst) {
                    Node rootNode = trees[id];
                    total += (int)calDifferentColor(id, rootNode.color, rootNode.lastUpdate)[0];
                }

                bw.write(total + "\n");
            }
        }

        bw.flush();
    }

    private static boolean isAbleToAdd(int pId) {
        Node parent = trees[pId];
        if(parent.childs.size() + 1 == parent.maxDepth) {      // 현재 자식을 최대로 가졌다면(자기 자신을 포함하니까 +1)
            return false;
        }
        return true;
    }

    /*
        subtree의 색깔을 모두 변경하는 대신 lastUpdate로 언제 변경했는지 저장해둠
     */
    private static void changeColor(int mId, int color, int lastUpdate) {
        Node node = trees[mId];
        node.color = color;
        node.lastUpdate = lastUpdate;
    }

    private static int[] getColor(int mId) {
        if(mId == -1) {
            return new int[]{0, 0};
        }
        Node cur = trees[mId];
        int[] parentNode = getColor(cur.pId);     // 조상 노드까지 재귀호출

        // lastUpdate가 더 최신인 노드를 반환
        if(parentNode[1] > cur.lastUpdate) {
            return parentNode;
        }
        return new int[]{cur.color, cur.lastUpdate};
    }

    private static Object[] calDifferentColor(int mId, int color, int lastUpdate) {
        Node cur = trees[mId];
        if(cur.lastUpdate > lastUpdate) {    // 업데이트가 필요함
            lastUpdate = cur.lastUpdate;
            color = cur.color;
        }

        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.cnt[color] = 1;    // 현재 색깔 갯수 포함시키기
        int total = 0;      // 가치의 합
        for(int child : cur.childs) {   // 자식 노드 번호
            Object[] resultNodeInfo = calDifferentColor(child, color, lastUpdate);
            nodeInfo = nodeInfo.updateVisit((NodeInfo) resultNodeInfo[1]);     // 색깔 추가
            total += (int)resultNodeInfo[0];
        }
        total += nodeInfo.calValue();    // 제곱
        return new Object[]{total, nodeInfo};
    }
}