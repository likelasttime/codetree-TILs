import java.util.*;
import java.io.*;

public class Main {
    final static int ARR_SIZE = 5;      // 유적지 크기
    final static int ROTATE_SIZE = 3;       // 회전시킬 격자 크기
    final static int[] DX = {-1, 1, 0, 0};
    final static int[] DY = {0, 0, -1, 1};

    static int k;       // 1 <= 턴수 <= 10
    static int m;       // 10 <= 벽면에 적힌 유물 조각의 개수 <= 300
    static int[][] arr;     // 유적지
    static int[][] rotate;  // 3 * 3 격자를 골라 회전시킨 유적지
    static int[] treasure;  // m개의 유물 조각 번호
    static int treasureIdx;     // treasure 배열 인덱스
    static List<Position> firstTreasure;    // 1차로 획득한 유물의 위치 저장
    static List<Position> tmpTreasure;      // 한턴에서 연쇄적으로 유물을 획득할 때마다 유물의 위치 저장

    static class Position implements Comparable<Position> {
        int x;      // 행
        int y;      // 열

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Position position) {
            if(this.y != position.y) {
                // 열 번호 오름차순
                return this.y - position.y;
            }
            return position.x - this.x;     // 행 번호 내림차순
        }
    }

    /*
        (startX, startY)에서 시작하는 3 * 3 격자를 시계방향으로 회전
    */
    public static void rotateArr(int startX, int startY, int[][] original) {
        int[][] copiedArr = new int[ARR_SIZE][ARR_SIZE];
        copied(copiedArr, original);       // result를 original로 깊은 복사
        int col = startY;
        int row;
        for(int i=startX; i<startX+ROTATE_SIZE; i++) {  // 행
            row = startX + ROTATE_SIZE - 1;
            for(int j=startY; j<startY+ROTATE_SIZE; j++) {      // 열
                original[i][j] = copiedArr[row--][col];
            }
            col++;
        }
    }

    /*
        arr1을 arr2로 깊은 복사
    */
    public static void copied(int[][] arr1, int[][] arr2) {
        for(int i=0; i<ARR_SIZE; i++) {
            for(int j=0; j<ARR_SIZE; j++) {
                arr1[i][j] = arr2[i][j];
            }
        }
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < ARR_SIZE && 0 <= y && y < ARR_SIZE;
    }

    /*
        유물 연쇄 획득
    */
    public static void getContinuousTreasure(int[][] tmp) {
        firstTreasure = new ArrayList();
        while(true) {
            boolean[][] visit = new boolean[ARR_SIZE][ARR_SIZE];
            tmpTreasure = new ArrayList();
            for(int i=0; i<ARR_SIZE; i++) {
                for(int j=0; j<ARR_SIZE; j++) {
                    List<Position> deletePos = bfs(i, j, tmp, visit);
                    if(deletePos.size() < 3) {
                        continue;
                    }
                    firstTreasure.addAll(deletePos);
                    tmpTreasure.addAll(deletePos);
                }
            }
            if(tmpTreasure.isEmpty()) {
                return;
            }
            getNewTreasure(tmp);
        }
    }

    /*
        유물이 사라진 자리에 새로운 유물 조각을 채우기
    */
    public static void getNewTreasure(int[][] tmp) {
        Collections.sort(tmpTreasure);
        for(Position pos : tmpTreasure) {
            tmp[pos.x][pos.y] = treasure[treasureIdx++];
        }
    }
    
    /*
        유물 가치 계산
     */
    public static void calTreasure(int[][] tmp) {
        boolean[][] visit = new boolean[ARR_SIZE][ARR_SIZE];
        List<Position> bfsResult = new ArrayList();
        
        for(int row=0; row<ARR_SIZE; row++) {
            for(int col=0; col<ARR_SIZE; col++) {
                if(visit[row][col]) {
                    continue;
                }
                List<Position> tmpResult = bfs(row, col, tmp, visit);
                if(tmpResult.size() < 3) {
                    continue;
                }
                bfsResult.addAll(tmpResult);
            }
        }
        
        if(firstTreasure.size() < bfsResult.size()) {  // 기존보다 유물의 가치가 더 높으면
            firstTreasure = bfsResult;
            copied(rotate, tmp);
        }
    }

    /*
        탐사 진행
    */
    public static void exploration() {
        for(int i=0; i<4; i++) {        // 시계방향 회전 횟수(90도, 180도, 270도)
            for(int y=0; y<=ARR_SIZE-3; y++) {          // 열
                for(int x=0; x<=ARR_SIZE-3; x++) {      // 행
                    int[][] tmp = new int[ARR_SIZE][ARR_SIZE];
                    copied(tmp, arr);
                    for(int d=0; d<i; d++) {       // 회전 횟수만큼 반복
                        rotateArr(x, y, tmp);
                    }
                    // 유물 가치 계산
                    calTreasure(tmp);
                }
            }
        }
    }

    /*
        (startX, startY)에서 시작해서 인접한 같은 종류의 유물 조각들을 찾아 반환
        tmp: 5 * 5 격재 내에서 3 * 3 격자를 회전시킨 임시 배열
    */
    public static List<Position> bfs(int startX, int startY, int[][] tmp, boolean[][] visit) {
        Queue<Position> que = new LinkedList();
        List<Position> result = new ArrayList();
        que.offer(new Position(startX, startY));
        result.add(new Position(startX, startY));
        visit[startX][startY] = true;

        while(!que.isEmpty()) {
            Position cur = que.poll();
            for(int d=0; d<4; d++) {
                int nx = cur.x + DX[d];
                int ny = cur.y + DY[d];
                // 격자를 벗어나거나 다른 유물 조각이거나 이미 방문했거나
                if(!isValid(nx, ny) || tmp[startX][startY] != tmp[nx][ny] || visit[nx][ny]) {
                    continue;
                }
                visit[nx][ny] = true;
                que.offer(new Position(nx, ny));
                result.add(new Position(nx, ny));
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        k = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());

        // 유물의 각 행에 있는 유물 조각에 적혀있는 숫자들 입력받기
        arr = new int[ARR_SIZE][ARR_SIZE];
        rotate = new int[ARR_SIZE][ARR_SIZE];
        for(int i=0; i<ARR_SIZE; i++) {
            st = new StringTokenizer(br.readLine());
            for(int j=0; j<ARR_SIZE; j++) {
                arr[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // 유물 조각 번호 입력받기
        treasure = new int[m];
        st = new StringTokenizer(br.readLine());
        for(int i=0; i<m; i++) {
            treasure[i] = Integer.parseInt(st.nextToken());
        }

        for(int turn=1; turn<=k; turn++) {
            firstTreasure = new ArrayList();
            tmpTreasure = new ArrayList();
            exploration();      // 탐사 진행
            if(firstTreasure.isEmpty()) {       // 유물을 더이상 획득할 방법이 없다면
                return;
            }
            getNewTreasure(rotate);
            getContinuousTreasure(rotate);      // 연쇄 유물 획득
            System.out.print(firstTreasure.size() + " ");       // 가치 출력
            copied(arr, rotate);
        }
    }
}
