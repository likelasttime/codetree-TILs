import java.io.*;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

public class Main {
    // 우하좌상
    static final int[] DX = new int[] {0, 1, 0, -1};
    static final int[] DY = new int[] {1, 0, -1, 0};
    // 좌하우상(나선형에서 사용)
    static final int[] SPIRALDX = new int[]{0, 1, 0, -1};
    static final int[] SPIRALDY = new int[]{-1, 0, 1, 0};

    static int[][] arr;
    static int midX;
    static int midY;
    static List<Node> nodeLst;
    static int answer = 0;      // 죽인 몬스터의 수
    static int n;       // 7 <= 격자 크기 <= 25

    // 좌표 클래스
    static class Node {
        int x;
        int y;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());   // 7 <= 격자 크기 <= 25
        int m = Integer.parseInt(st.nextToken());      // 1 <= 총 라운드 수 <= 100
        arr = new int[n][n];
        midX = n / 2;
        midY = n / 2;
        nodeLst = new ArrayList();
        // 몬스터의 종류 입력받기
        for(int i=0; i<n; i++) {
            st = new StringTokenizer(br.readLine());
            for(int j=0; j<n; j++) {
                arr[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        spiral();       // 나선 방향으로 탐색

        // 각 라운드마다의 플레이어의 공격 방향, 공격 칸 수를 입력받기
        for(int i=0; i<m; i++) {
            st = new StringTokenizer(br.readLine());
            int d = Integer.parseInt(st.nextToken());       // 0 <= 공격 방향 <= 3
            int p = Integer.parseInt(st.nextToken());       // 1 <= 공격 칸 수 <= floor(n/2)

            attack(d, p);    // 몬스터 제거
            pull();     // 빈 공간 채우기
            removeAndPull();    // 4번 이상 반복하여 나오는 몬스터 제거 후 당기기 작업 반복
            insert();       // 같은 숫자끼리 짝을 지어서 (총 개수, 숫자의 크기)를 미로에 넣기
        }

        System.out.println(answer);       // 플레이어가 얻은 점수 출력
    }

    /*
        d: 공격 방향
        p: 공격 칸 수
    */
    public static void attack(int d, int p) {
        int depth = 0;      // 죽인 몬스터의 수

        int x = midX, y = midY;     // 좌표(중심점에서 시작)

        while(depth < p) {      // p만큼 이동
            x += DX[d];
            y += DY[d];
            answer += arr[x][y];  // 몬스터를 죽여서 점수를 얻음
            arr[x][y] = 0;      // 몬스터 죽이기
            depth++;        // 죽인 몬스터의 수 증가
        }
    }

    /*
        중심점(midX, midY)에서 나선 방향으로 탐색하면서 차례로 리스트 nodeLst에 기록하기
        나선 방향으로 탐색할 때 nodeLst에 있는 값을 순차적으로 꺼내서 사용할 것이다.
    */
    public static void spiral() {
        int depth = 1;      // 움직일 칸 수
        int dir = 0;        // 방향
        int x = midX;
        int y = midY;

        while(x > 0 || y > 0) {     // 왜 or이지
            for(int cnt = 0; cnt<depth; cnt++) {        // 움직일 칸 수만큼
                x += SPIRALDX[dir];
                y += SPIRALDY[dir];
                nodeLst.add(new Node(x, y));

                if(x == 0 && y == 0) {      // (0, 0)이라면 끝난다
                    break;
                }
            }

            dir = (dir + 1) % 4;        // 방향 바꾸기

            if(dir == 0 || dir == 2) {      // 좌 또는 우 방향이면 1칸 더 가야 함
                depth++;        // 움질일 칸 수 증가
            }
        }
    }

    /*
        4번 이상 반복하여 나오는 구간을 지우고, 당기기를 반복
    */
    public static void removeAndPull() {
        while(true) {       // 4번 이상 반복하여 나오는 구간을 모두 지울때 까지
            if(!remove()) {     // 4번 이상 반복되는 구간이 하나도 없었다면
                break;
            }

            pull();     // 빈 공간 채우기
        }
    }

    /*
        빈곳(0)을 없애기 위한 당기기
     */
    public static void pull() {
        int[][] temp = new int[n][n];       // 임시배열
        int idx = 0;

        for(Node node : nodeLst) {      // 중점에서 시작해서 나선 방향으로 탐색
            int x = node.x;
            int y = node.y;

            if(arr[x][y] != 0) {        // 몬스터가 있다면
                Node tempNode = nodeLst.get(idx);
                temp[tempNode.x][tempNode.y] = arr[x][y];       // 몬스터 넣기
                idx++;
            }
        }

        // temp 값을 arr 배열에 하나씩 복사
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                arr[i][j] = temp[i][j];
            }
        }
    }

    /*
        삭제가 끝난 후 같은 숫자끼리 짝을 지어서 (총 개수, 숫자의 크기)를 미로에 넣기
    */
    public static void insert() {
        int start = 0;      // 같은 몬스터를 찾기 위한 시작점
        int end = start + 1;        // 같은 몬스터를 찾기 위한 종료점
        int nodeLstIdx = 0;         
        int tmpLstIdx = 0;
        List<Integer> tmpLst = new ArrayList();     // (총 개수, 몬스터 종류)를 담는 리스트
        int[][] tmp = new int[n][n];    // arr 배열에 값을 할당하기 전에 사용할 임시 배열

        while(start < nodeLst.size() && tmpLst.size() <= nodeLst.size()) {        // 끝까지 탐색
            int startMonster = getMonster(start);       // 시작점 몬스터 종류

            if(startMonster == 0) {     // 몬스터가 더이상 없어서 탐색을 종료
                break;
            }

            while(end < nodeLst.size()) {       // 시작점 몬스터와 같은 종류가 연속으로 나올 때까지
                int endMonster = getMonster(end);       // 마지막 지점 몬스터의 종류

                if(endMonster == startMonster) {        // 연속으로 같은 몬스터라면
                    end++;      // 종료점을 1 늘리기
                } else {    // 다른 몬스터라면
                    break;
                }
            }
            
            tmpLst.add(end - start);        // 총 개수

            if(tmpLst.size() == nodeLst.size()) {       // 정해진 배열의 사이즈를 넘어가면
                break;
            }

            tmpLst.add(startMonster);       // 몬스터 종류
            start = end;        // 그 다음 시작점
        }

        // 임시 배열에 값 할당
        while(nodeLstIdx < nodeLst.size() && tmpLstIdx < tmpLst.size()) {   // 인덱스를 초과하지 않을 동안에 반복
            Node curNode = nodeLst.get(nodeLstIdx);
            tmp[curNode.x][curNode.y] = tmpLst.get(tmpLstIdx);
            nodeLstIdx++;
            tmpLstIdx++;
        }

        // 원본 배열 arr에 값 할당
        for(int x=0; x<n; x++) {
            for(int y=0; y<n; y++) {
                arr[x][y] = tmp[x][y];
            }
        }
    }

    /*
        4번 이상 반복되는 구간을 찾아서 지우기
        isRemoved가 true면 당기기 작업을 위해 pull 메소드를 호출해야해서 사용
    */
    public static boolean remove() {
        int start = 0;
        int end = nodeLst.size();
        boolean isRemoved = false;

        while(start < end) {        // 끝까지 탐색
            int sameMonsterEnd = getEndSameMonsterIdx(start, end);  // start에서 시작해서 연속으로 같은 몬스터가 있는 종료 인덱스

            if(getMonster(start) == 0) {      // 몬스터가 없다면
                break;
            }

            if(sameMonsterEnd - start + 1 >= 4) {      // 4번 이상 반복하여 같은 몬스터가 나왔다면
                for(int removeIdx=start; removeIdx<=sameMonsterEnd; removeIdx++) {   // start ~ sameMonsterEnd까지
                    Node node = nodeLst.get(removeIdx);
                    answer += arr[node.x][node.y];      // 몬스터를 죽여서 점수를 얻음
                    arr[node.x][node.y] = 0;        // 해당 위치에 있는 몬스터 제거
                }
                isRemoved = true;
            }

            start = sameMonsterEnd + 1;     // 그 다음 시작 값 갱신
        }
        return isRemoved;
    }

    /*
        start에서 시작해서 같은 몬스터가 연속으로 나오는 종료 인덱스 찾기
    */
    public static int getEndSameMonsterIdx(int start, int end) {
        int startMonster = getMonster(start);       // 시작 지점 몬스터 종류
        int cur = start + 1;    // 연속으로 같은 몬스터가 나오는 종료 인덱스

        while(cur < end) {    // 모든 몬스터 탐색
            int monster = getMonster(cur);      // 현재 몬스터의 종류

            if(monster == startMonster) {      // 연속으로 같은 몬스터 발견
                cur++;      
            } else {    // 몬스터 종류가 다르다면 탐색 종료
                break;
            }
        }
        return cur - 1;
    }

    /*
        몬스터 종류 반환
    */
    public static int getMonster(int idx) {
        Node node = nodeLst.get(idx);
        return arr[node.x][node.y];
    }
}