import java.io.*;
import java.util.StringTokenizer;
import java.util.PriorityQueue;
import java.util.Arrays;

public class Main {
    static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Santa implements Comparable<Santa> {
        int score;      // 점수
        Position position;      // (행, 열)
        int distance;   // 루돌프와 거리

        Santa(int score, Position position, int distance) {
            this.score = score;
            this.position = position;
            this.distance = distance;
        }

        @Override
        public int compareTo(Santa s) {
            if(this.distance == s.distance) {  // 거리가 같으면
                if(this.position.x == s.position.x) {      // 행이 같으면
                    return Integer.compare(s.position.y, this.position.y);     // 열이 큰 순으로
                }
                return Integer.compare(s.position.x, this.position.x);     // 행이 큰 순으로
            }
            return Integer.compare(this.distance, s.distance);    // 거리가 가까운 순
        }
    }

    final static int[] DX = new int[]{-1, 0, 1, 0, -1, -1, 1, 1};
    final static int[] DY = new int[]{0, 1, 0, -1, -1, 1, -1, 1};

    static int N;       // 3 <= 게임판의 크기 <= 50
    static int M;       // 1 <= 게임 턴 수 <= 1000
    static int P;       // 1 <= 산타의 수 <= 30
    static int C;       // 1 <= 루돌프의 힘 <= N
    static int D;       // 1 <= 산타의 힘 <= N
    static int[] sleep;     // 기절한 당시 턴
    static Santa[] santas;
    static int[][] matrix;      // N * N 크기의 게임판
    static boolean[] isGameOver;    // 게임에서 탈락한 산타인지
    static PriorityQueue<Santa> pq = new PriorityQueue();
    static Position deerPos;        // 루돌프의 위치
    static int turn = 1;        // 현재 턴 수

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        P = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        D = Integer.parseInt(st.nextToken());
        matrix = new int[N + 1][N + 1];     // 좌상단이 (1, 1)로 시작해서 +1만큼 할당
        isGameOver = new boolean[P + 1];    // 산타 번호가 1부터 시작이니 +1만큼 할당
        sleep = new int[P + 1];
        Arrays.fill(sleep, -1);

        // 루돌프의 초기 위치 입력 받기
        st = new StringTokenizer(br.readLine());
        int rx = Integer.parseInt(st.nextToken());
        int ry = Integer.parseInt(st.nextToken());
        deerPos = new Position(rx, ry);     // 루돌프의 위치 할당
        matrix[rx][ry] = -1;        // 루돌프 저장

        // 산타의 번호와 초기 위치 입력 받기
        santas = new Santa[P + 1];  // 산타의 번호는 1번부터 시작해서 +1한 길이를 할당
        for(int p=1; p<=P; p++) {
            st = new StringTokenizer(br.readLine());
            int pn = Integer.parseInt(st.nextToken());      // 산타 번호
            int sr = Integer.parseInt(st.nextToken());      // 초기 위치 행
            int sc = Integer.parseInt(st.nextToken());      // 초기 위치 열

            santas[pn] = new Santa(0, new Position(sr, sc), 0);
            matrix[sr][sc] = pn;        // 산타 저장
        }

        // M개의 턴을 걸쳐서 게임을 진행
        for(int m=0; m<M; m++) {
            // 루돌프가 움직인다.
            moveDeer();

            // 1번 산타부터 P번 산타까지 순서대로 움직인다.
            moveSanta();

            // 현재 턴 종료
            if(!endTurn()) {        // 산타가 다 죽음
                break;
            }

            turn++; //  현재 턴 수
        }

        // 각 산타가 얻은 최종 점수를 출력
        printScore(bw);
        bw.flush();
    }

    /*
        각 산타가 얻은 최종 점수를 출력
    */
    public static void printScore(BufferedWriter bw) throws IOException {
        for(int p=1; p<=P; p++) {
            bw.write(santas[p].score + " ");
        }
    }

    /*
        턴이 종료될 때마다 탈락하지 않은 산타들에게 1점씩 추가 부여
    */
    public static boolean endTurn() {
        boolean flag = false;       
        
        for(int p=1; p<=P; p++) {       // 산타 번호
            if(isGameOver[p]) {     // 탈락한 산타라면
                continue;
            }

            flag = true;    // 살아남은 산타가 있다.
            santas[p].score += 1;   // 1점 부여
        }
        return flag;
    }

    /*
        루돌프가 가장 가까운 산타를 향해 1칸 돌진
    */
    public static void moveDeer() {
        pq.clear();     // 우선순위 큐 초기화

        // 루돌프와의 거리를 계산해서 산타를 우선순위 큐에 넣기
        for(int p=1; p<=P; p++) {
            if(isGameOver[p]) {     // 게임에서 탈락한 산타라면
                continue;
            }

            Santa santa = santas[p];    // p번째 산타
            int distance = calDistance(deerPos.x, deerPos.y, santa.position.x, santa.position.y);  // 루돌프와 산타 간의 거리 계산
            pq.offer(new Santa(0, new Position(santa.position.x, santa.position.y), distance));
        }
        Santa santa = pq.poll();        // 가장 우선순위가 높은 산타
        int santaId = matrix[santa.position.x][santa.position.y];

        // 8방향 중 가장 가까워지는 방향으로 산타에게 한 칸 돌진
        int dx = 0;
        int dy = 0;

        if(santa.position.x > deerPos.x) {      // 루돌프의 행이 더 작으면
            dx = 1;     // 루돌프의 행을 증가시키기
        } else if(santa.position.x < deerPos.x) {       // 루돌프의 행이 더 크면
            dx = -1;        // 루돌프의 행을 감소시키기
        }

        if(santa.position.y > deerPos.y) {      // 루돌프의 열이 더 작으면
            dy = 1;     // 열을 증가시키기
        } else if(santa.position.y < deerPos.y) {       // 루돌프의 열이 더 크면
            dy = -1;    // 열을 감소시키기
        }

        // 루돌프 이동
        int nx = deerPos.x + dx;
        int ny = deerPos.y + dy;
        matrix[deerPos.x][deerPos.y] = 0;       // 빈 칸으로 만들기

        // 루돌프가 새 위치(nx, ny)로 이동했는데 기존에 있던 산타랑 충돌
        if(nx == santa.position.x && ny == santa.position.y) {
            int beforeX = nx + (dx * C);       // 시작 행
            int beforeY = ny + (dy * C);       // 시작 열
            int afterX = beforeX;        // 도착 행
            int afterY = beforeY;        // 도착 열

            // 인덱스가 유효하고 (afterX, afterY)에 산타가 있으면, (dx, dy) 방향으로 가기
            while(isValid(afterX, afterY) && matrix[afterX][afterY] > 0) {  
                afterX += dx;
                afterY += dy;
            }

            // 마지막 충돌이 일어났던 지점부터 다시 거꾸로
            while(afterX != beforeX && afterY != beforeY) {        
                int tmpX = afterX - dx;
                int tmpY = afterY - dy;

                if(isValid(tmpX, tmpY)) {      // 범위를 벗어났다면
                    break;
                }

                int id = matrix[tmpX][tmpY];        // 산타 번호

                if(!isValid(afterX, afterY)) {      // 범위를 벗어나면
                    isGameOver[id] = true;      // 탈락 처리
                } else {        // id번째 산타가 (afterX, afterY) 위치로 이동
                    matrix[afterX][afterY] = matrix[beforeX][beforeY];
                    santas[id].position = new Position(afterX, afterY);
                }

                afterX = tmpX;
                afterY = tmpY;
            }

            santas[santaId].score += C;      // 점수 부여
            santas[santaId].position = new Position(beforeX, beforeY);      // 산타의 위치 갱신
            sleep[santaId] = turn;      // 산타가 기절

            if(isValid(beforeX, beforeY)) {     // 인덱스가 유효하다면
                matrix[beforeX][beforeY] = santaId;
            } else {
                isGameOver[santaId] = true;     // 산타 죽음
            }
        }

        matrix[nx][ny] = -1;      // 루돌프 위치 할당
        deerPos = new Position(nx, ny);
    }

    /*
        산타가 1번부터 P번까지 순서대로 움직인다.
    */
    public static void moveSanta() {
        for(int p=1; p<=P; p++) {       // 산타의 번호
            if(isGameOver[p] || sleep[p] == turn || sleep[p] + 1 == turn) {      // 기절했거나 이미 게임에서 탈락한 산타라면
                continue;
            }

            int santaPosX = santas[p].position.x;   // p번째 산타의 위치 행
            int santaPosY = santas[p].position.y;   // p번째 산타의 위치 열
            int curDistance = calDistance(deerPos.x, deerPos.y, santaPosX, santaPosY);      // 현재 위치에서 루돌프와의 거리
            int moveDir = -1;       // 움직일 방향

            // 상우하좌 우선순위로 움직이기
            Position newPos = new Position(santaPosX, santaPosY);
            for(int d=0; d<4; d++) {
                int nx = DX[d] + santaPosX;
                int ny = DY[d] + santaPosY;

                if(!isValid(nx, ny)) {   // 좌표가 유효하지 않다면
                    continue;
                } else if(matrix[nx][ny] > 0) {      // 다른 산타가 있는 칸이라면
                    continue;
                }

                int newDistance = calDistance(deerPos.x, deerPos.y, nx, ny);        // 거리 계산
                if(curDistance > newDistance) {     // 루돌프에게 가까워지는 방향을 찾음
                    curDistance = newDistance;  // 거리 갱신
                    moveDir = d;        // 방향 갱신
                }
            }

            if(moveDir != -1) {     // 루돌프에게 가까워지는 방향을 찾았다면
                int nx = santas[p].position.x + DX[moveDir];
                int ny = santas[p].position.y + DY[moveDir];

                if(matrix[nx][ny] == -1) {      // 충돌이 일어났다면
                    sleep[p] = turn;             // 기절
                    int moveX = -DX[moveDir];
                    int moveY = -DY[moveDir];
                    int firstX = nx + moveX * D;
                    int firstY = ny + moveY * D;
                    int lastX = firstX;
                    int lastY = firstY;

                    // 밀려난 칸에 다른 산타가 있을 동안에
                    while(isValid(lastX, lastY) && matrix[lastX][lastY] > 0) {  // 좌표가 유효하고, 산타가 있다면
                        lastX += moveX;
                        lastY += moveY;
                    }

                    // 가장 마지막 위치에서 시작해 순차적으로 산타를 한칸씩 이동시키기
                    while(!(lastX == firstX && lastY == firstY)) {
                        int beforeX = lastX - moveX;
                        int beforeY = lastY - moveY;

                        if(!isValid(beforeX, beforeY)) {        // 유효하지 않는 좌표라면
                            break;
                        }

                        int idx = matrix[beforeX][beforeY];

                        if(!isValid(lastX, lastY)) {        // 좌표가 유효하지 않으면
                            isGameOver[idx] = true;     // 탈락
                        } else {
                            matrix[lastX][lastY] = matrix[beforeX][beforeY];
                            santas[idx].position = new Position(lastX, lastY);      // 위치 갱신
                        }

                        lastX = beforeX;
                        lastY = beforeY;
                    }

                    santas[p].score += D;   // 점수 얻기
                    matrix[santas[p].position.x][santas[p].position.y] = 0;     // 자리 비움
                    santas[p].position = new Position(firstX, firstY);     // 위치 갱신

                    if(isValid(firstX, firstY)) {       // 좌표가 유효하면
                        matrix[firstX][firstY] = p;     // p번 산타가 (firstX, firstY) 좌표에 앉음
                    } else {
                        isGameOver[p] = true;      // p번 산타가 게임에서 탈락
                    }
                } else {
                    matrix[santas[p].position.x][santas[p].position.y] = 0;     // 자리 비움
                    santas[p].position = new Position(nx, ny);
                    matrix[nx][ny] = p;     // p번 산타가 (nx, ny)에 앉음
                }
            }
        }
    }

    /*
        현재 좌표가 유효하다면 true 반환
    */
    public static boolean isValid(int x, int y) {
        return 1 <= x && x <= N && 1 <= y && y <= N; 
    }

    /*
        두 위치 간의 거리 계산
    */
    public static int calDistance(int r1, int c1, int r2, int c2) {
        return (int)(Math.pow(Math.abs(r1 - r2), 2) + Math.pow(Math.abs(c1 - c2), 2));
    }
}