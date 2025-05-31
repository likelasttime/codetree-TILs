import java.util.*;
import java.io.*;

public class Main {
    final static int[] DX = {-1, 1, 0, 0};
    final static int[] DY = {0, 0, -1, 1};

    static int n;       // 2 <= 격자 크기 <= 15
    static int q;
    static int curQ;    // 현재 진행된 실험 횟수
    static int[][] arr;     // n * n 크기의 격자
    static int[] microorganismSize;     // 미생물 크기를 저장하는 배열

    static class Position implements Comparable<Position> {
        int x;      // 열
        int y;      // 행

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Position position) {
            if(this.y != position.y) {
                return position.y - this.y;     // 내림차순
            }
            return this.x - position.x;     // 오름차순
        }
    }

    static class Microorganism implements Comparable<Microorganism> {
        int size;
        int num;

        Microorganism(int size, int num) {
            this.size = size;
            this.num = num;
        }

        @Override
        public int compareTo(Microorganism microorganism) {
            if(this.size != microorganism.size) {
                return microorganism.size - this.size;      // 영역 크기를 기준으로 내림차순
            }
            return this.num - microorganism.num;    // 빨리 투입된 순
        }
    }

    /*
        1) 미생물 투입
    */
    public static void inputMicroorganism(int r1, int c1, int r2, int c2) {
        Set<Integer> dieSet = new HashSet();        // 죽은 미생물 번호를 저장
        for(int y=c2; y<=c1; y++) {      // 세로(행)
            for(int x=r1; x<=r2; x++) {      // 가로(열)
                if(arr[y][x] != 0) {        // 기존에 미생물이 있던 칸
                    int prev = arr[y][x];       // 기존 미생물 번호
                    microorganismSize[prev]--;      // 기존 미생물 무리의 영역 크기 감소'
                    microorganismSize[curQ]++;      // 투입된 미생물 무리의 영역 크기 증가
                    arr[y][x] = curQ;    // 새로운 미생물 투입
                    dieSet.add(prev);       // 죽은 미생물 번호 추가
                } else {
                    arr[y][x] = curQ;        // 빈칸에 미생물 투입
                    microorganismSize[curQ]++;      // 미생물 영역 크기 증가
                }
            }
        }

        for(int num : dieSet) {     // 죽은 미생물 번호
            boolean[][] visit = new boolean[n][n];
            int count = 0;      // countGroupBfs 호출 횟수
            for(int y=0; y<n; y++) {
                for(int x=0; x<n; x++) {
                    // 이미 방문했거나 찾는 미생물이 아니거나
                    if(visit[y][x] || arr[y][x] != num) {
                        continue;
                    }
                    countGroupBfs(y, x, arr[y][x], visit);
                    count++;
                }
            }
            if(count > 1) {     // 미생물이 차지한 영역이 둘 이상으로 나뉘었다면
                // 배양용기에서 모두 사라진다
                die(num);
            }
        }

    }

    /*
        2) 배양 용기 이동
     */
    public static void moveAll() {
        int[][] newMatrix = new int[n][n];      // 새로운 배양 용기
        // 새로운 배양 용기로 옮기는 순서를 결정
        PriorityQueue<Microorganism> pq = new PriorityQueue();
        for(int i=1; i<=curQ; i++) {        // 미생물 번호
            if(microorganismSize[i] > 0) {      // 배양용기에 남아있는 미생물이면
                pq.offer(new Microorganism(microorganismSize[i], i));
            }
        }
        // 현재 배양용기에 있는 모든 미생물을 옮길때까지
        while(!pq.isEmpty()) {
            Microorganism microorganism = pq.poll();
            boolean isSuccess = move(newMatrix, microorganism.num);
            if(!isSuccess) {        // 미생물을 못 옮겼다면
                // 배양용기에서 모두 사라진다
                die(microorganism.num);
            }
        }
        // 새로운 배양 용기로 갱신
        for(int y=0; y<n; y++) {    // 행(세로)
            for(int x=0; x<n; x++) {    // 열(가로)
                arr[y][x] = newMatrix[y][x];
            }
        }
    }

    /*
        num 미생물과 인접한 미생물을 찾기
     */
    public static boolean[] findNearBfs(int y, int x) {
        boolean[] microorganismVisit = new boolean[curQ + 1];       // 인접한 미생물 체크 배열
        boolean[][] visit = new boolean[n][n];
        Queue<Position> que = new LinkedList();
        que.offer(new Position(x, y));
        microorganismVisit[arr[y][x]] = true;
        visit[y][x] = true;

        while(!que.isEmpty()) {
            Position cur = que.poll();
            for(int d=0; d<4; d++) {
                int nx = DX[d] + cur.x;
                int ny = DY[d] + cur.y;
                // 격자 바깥을 벗어나거나 이미 방문했거나 빈칸이면
                if(!isValid(nx, ny) || visit[ny][nx] || arr[ny][nx] == 0) {
                    continue;
                }
                visit[ny][nx] = true;
                microorganismVisit[arr[ny][nx]] = true;
                if(arr[ny][nx] == arr[y][x]) {
                    que.offer(new Position(nx, ny));    // 같은 미생물 군집만 큐에 추가
                }
            }
        }
        return microorganismVisit;
    }

    /*
        3) 실험 결과 기록
     */
    public static int getResult() {
        int result = 0;
        boolean[] visit = new boolean[curQ + 1];
        boolean[][] pair = new boolean[curQ + 1][curQ + 1];

        for(int y=0; y<n; y++) {        // 세로(행)
            for(int x=0; x<n; x++) {    // 가로(열)
                if(arr[y][x] == 0 || visit[arr[y][x]]) {    // 미생물이 없는 곳이거나 이미 bfs 탐색한 미생물이면
                    continue;
                }
                visit[arr[y][x]] = true;
                boolean[] nearVisit = findNearBfs(y, x);
                for(int i=1; i<=curQ; i++) {
                    if(i == arr[y][x]) {
                        continue;
                    }
                    if(nearVisit[i] && !pair[arr[y][x]][i] && !pair[i][arr[y][x]]) {      // 인접한 미생물이라면
                        pair[arr[y][x]][i] = true;
                        pair[i][arr[y][x]] = true;
                        result += (microorganismSize[i] * microorganismSize[arr[y][x]]);
                    }
                }
            }
        }
        return result;
    }

    public static boolean move(int[][] newMatrix, int num) {
        for(int x=0; x<n; x++) {
            for(int y=n-1; y>=0; y--) {
                // 이미 다른 미생물이 있는 자리는 시작점이 될 수 없다
                if(newMatrix[y][x] != 0) {
                    continue;
                }
                // 현재 배양 용기에서 새로운 배양 용기로 옮기기
                boolean isSuccess = moveSimulation(y, x, num, newMatrix);
                if(isSuccess) {
                    return true;
                }
            }
        }
        return false;       // 미생물을 옮기지 못 했을 때
    }

    /*
        num: 찾을 미생물 번호
     */
    public static PriorityQueue<Position> getPositions(int num) {
        PriorityQueue<Position> positions = new PriorityQueue();
        for(int y=0; y<n; y++) {        // 행(세로)
            for(int x=0; x<n; x++) {    // 열(가로)
                if(arr[y][x] == num) {
                    positions.add(new Position(x, y));
                }
            }
        }
        return positions;
    }

    public static int[][] copied(int[][] newMatrix) {
        int[][] tmp = new int[n][n];
        for(int y=0; y<n; y++) {    // 행(세로)
            for(int x=0; x<n; x++) {    // 열(가로)
                tmp[y][x] = newMatrix[y][x];
            }
        }
        return tmp;
    }

    /*
        startY: 행(세로)
        startX: 열(가로)
     */
    public static boolean moveSimulation(int startY, int startX, int num, int[][] newMatrix) {
        int[][] tmp = copied(newMatrix);
        PriorityQueue<Position> positions = getPositions(num);
        int diffRow = startY - positions.peek().y;      // 행(세로)
        int diffCol = startX - positions.peek().x;      // 열(가로)

        while(!positions.isEmpty()) {
            Position cur = positions.poll();
            int nx = cur.x + diffCol;
            int ny = cur.y + diffRow;
            if(!isValid(nx, ny) || newMatrix[ny][nx] != 0) {      // 배양용기를 벗어나거나 다른 미생물과 겹친다면
                return false;
            }
            tmp[ny][nx] = num;
        }
        for (int y=0; y<n; y++) {
            for (int x=0; x<n; x++) {
                newMatrix[y][x] = tmp[y][x];
            }
        }
        return true;
    }

    public static boolean isValid(int nx, int ny) {
        return 0 <= nx && nx < n && 0 <= ny && ny < n;
    }

    /*
        num: 죽일 미생물 번호
    */
    public static void die(int num) {
        for(int y=0; y<n; y++) {        // 행(세로)
            for(int x=0; x<n; x++) {    // 열(가로)
                if(arr[y][x] == num) {
                    arr[y][x] = 0;
                }
            }
        }
        microorganismSize[num] = 0;     // num번 미생물 영역의 크기를 0으로 초기화
    }

    /*
        기존에 있던 어떤 미생물 무리 A가 차지하던 영역이 둘 이상으로 나누어지었는지 탐색
        num: 미생물 번호
    */
    public static void countGroupBfs(int row, int col, int num, boolean[][] visit) {
        Queue<Position> que = new LinkedList();
        que.offer(new Position(col, row));
        visit[row][col] = true;
        while(!que.isEmpty()) {
            Position cur = que.poll();
            for(int d=0; d<4; d++) {
                int nx = DX[d] + cur.x;     // 가로(열)
                int ny = DY[d] + cur.y;     // 세로(행)
                if(!isValid(nx, ny) || visit[ny][nx] || arr[ny][nx] != num) {
                    continue;
                }
                visit[ny][nx] = true;
                que.offer(new Position(nx, ny));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        q = Integer.parseInt(st.nextToken());
        arr = new int[n][n];
        microorganismSize = new int[q + 1];
        // 미생물의 위치 정보 입력 받기
        for(curQ=1; curQ<=q; curQ++) {
            st = new StringTokenizer(br.readLine());
            // 좌측 하단 좌표
            int r1 = Integer.parseInt(st.nextToken());      // 가로(열)
            int c1 = Integer.parseInt(st.nextToken());      // 세로(행)
            // 우측 상단 좌표
            int r2 = Integer.parseInt(st.nextToken());      // 가로(열)
            int c2 = Integer.parseInt(st.nextToken());      // 세로(행)

            // 미생물 투입
            inputMicroorganism(r1, n - c1 - 1, r2 - 1, n - c2);
            // 배양 용기 이동
            moveAll();
            // 실험결과 기록
            System.out.println(getResult());
        }
    }
}