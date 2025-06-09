import java.util.*;
import java.io.*;

public class Main {
    static int n;       // 3 <= 게임판의 크기 <= 50
    static int m;       // 1 <= 게임 턴 수 <= 1,000
    static int p;       // 1 <= 산타의 수 <= 30
    static int c;       // 1 <= 루돌프의 힘 <= n
    static int d;       // 1 <= 산타의 힘 <= n
    static Position deer;       // 루돌프 위치
    static int[][] arr;
    static int turn;        // 현재 턴 수
    static boolean[] isDead;        // 게임에서 탈락한 산타
    static Position[] santaArr;     // 산타의 위치를 저장하는 배열
    static int[] sleepTimeArr;        // 산타가 기절한 시점을 저장하는 배열
    static int[] score;     // 각 산타별로 받은 점수를 저장하는 배열

    static class Position {
        int x;      // 행
        int y;      // 열

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Santa implements Comparable<Santa> {
        int x;       // 행
        int y;      // 열
        int distance;   // 거리

        Santa(int x, int y, int distance) {
            this.x = x;
            this.y = y;
            this.distance = distance;
        }

        @Override
        public int compareTo(Santa santa) {
            if(this.distance != santa.distance) {
                return this.distance - santa.distance;      // 거리 오름차순
            }
            if(this.x != santa.x) {
                return santa.x - this.x;        // 행 내림차순
            }
            return santa.y - this.y;        // 열 내림차순
        }
    }

    /*
        (x, y)가 격자 내에 있으면
    */
    public static boolean isValid(int x, int y) {
        return 1 <= x && x <= n && 1 <= y && y <= n;
    }

    public static int getDistance(int x1, int y1, int x2, int y2) {
        return ((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2));
    }

    /*
        루돌프 움직임
    */
    public static void deerMove() {
        int[] DX = {1, 1, 1, 0, 0, -1, -1, -1};
        int[] DY = {1, 0, -1, 1, -1, 1, 0, -1};
        int minDistance = Integer.MAX_VALUE;
        int moveD = -1;     // 루돌프가 움직인 방향
        Santa santa = selectSanta();        // 루돌프와 가장 가까운 산타
        Position santaPos = santaArr[arr[santa.x][santa.y]];     // 산타 위치

        // 산타를 향해 루돌프가 돌진할 위치 찾기
        int tmpX = deer.x;
        int tmpY = deer.y;
        for(int d=0; d<8; d++) {
            int nx = DX[d] + deer.x;
            int ny = DY[d] + deer.y;
            if(!isValid(nx, ny)) {      // 격자를 벗어나면
                continue;
            }
            int distance = getDistance(nx, ny, santaPos.x, santaPos.y);     // 루돌프와 산타의 거리 계산
            if(minDistance > distance) {      // 최소 거리라면
                minDistance = distance;
                moveD = d;      // 루돌프가 움직인 방향 갱신
                tmpX = nx;
                tmpY = ny;
            }
        }

        // 루돌프 위치 갱신
        deer.x = tmpX;
        deer.y = tmpY;

        if(arr[deer.x][deer.y] != 0) {      // 산타와 루돌프가 충돌했다면
            collision(DX[moveD], DY[moveD], c);
        }
    }

    /*
        루돌프가 돌진할 산타를 고르기
     */
    public static Santa selectSanta() {
        PriorityQueue<Santa> pq = new PriorityQueue();
        for(int i=1; i<=p; i++) {
            if (isDead[i]) {   // 게임에서 탈락한 산타라면
                continue;
            }
            Position santaPos = santaArr[i];        // 산타 위치
            int curDistance = getDistance(deer.x, deer.y, santaPos.x, santaPos.y);    // 현재 위치에서 루돌프와 거리
            pq.offer(new Santa(santaPos.x, santaPos.y, curDistance));
        }
        return pq.poll();
    }

    /*
        산타의 위치를 갱신
        (x, y): 원위치
        (nx, ny): 새위치
    */
    public static void move(int x, int y, int nx, int ny) {
        int num = arr[x][y];        // 산타 번호
        arr[x][y] = 0;        // 산타가 있던 자리를 초기화
        arr[nx][ny] = num;        // 새 위치에 산타 넣기
        santaArr[num] = new Position(nx, ny);     // 산타 위치 갱신
    }

    /*
        상호작용
        xD: 이동해 온 행 방향
        yD: 이동해 온 열 방향
        (startX, startY): 루돌프에게 부딪혀서 산타가 튕겨나간 위치
    */
    public static void interaction(int xD, int yD, int startX, int startY) {
        Stack<Position> stack = new Stack();
        stack.add(new Position(startX, startY));
        int depth = 1;
        while(true) {
            int nx = (xD * depth) + startX;
            int ny = (yD * depth) + startY;
            if(!isValid(nx, ny) || arr[nx][ny] == 0) {      // 격자 바깥을 벗어나거나 산타가 없으면
                break;
            }
            stack.add(new Position(nx, ny));
            depth++;
        }

        // 산타 밀려나기
        while(!stack.isEmpty()) {
            Position cur = stack.pop();        // 현재 위치
            int nx = cur.x + xD;
            int ny = cur.y + yD;
            if(!isValid(nx, ny)) {      // 격자 바깥을 벗어나면
                dead(cur.x, cur.y);
                continue;
            }
            move(cur.x, cur.y, nx, ny);     // 산타 이동
        }

        move(deer.x, deer.y, startX, startY);   // 루돌프와 부딪힌 산타를 C 또는 D만큼 이동
    }

    /*
        충돌
    */
    public static void collision(int xD, int yD, int bonus) {
        score[arr[deer.x][deer.y]] += bonus;        // 점수 획득
        sleepTimeArr[arr[deer.x][deer.y]] = turn + 1;       // 다음 턴까지 기절
        // bonus만큼 (xD, yD)방향으로 밀려나기
        int nx = (bonus * xD) + deer.x;
        int ny = (bonus * yD) + deer.y;
        if(!isValid(nx, ny)) {      // 격자 바깥을 벗어나면
            dead(deer.x, deer.y);
            return;
        }

        if(arr[nx][ny] != 0) {      // 밀려난 자리에 다른 산타가 있으면
            interaction(xD, yD, nx, ny);        // 연쇄적으로 산타들이 밀려난다
        } else {
            move(deer.x, deer.y, nx, ny);   // 루돌프와 부딪힌 산타만 밀려난다
        }
    }

    public static void dead(int x, int y) {
        isDead[arr[x][y]] = true;
        arr[x][y] = 0;      // 격자에서 산타 제거
    }

    /*
        산타 움직임
    */
    public static void santaMove() {
        // 상우하좌
        int[] DX = {-1, 0, 1, 0};
        int[] DY = {0, 1, 0, -1};
        for(int i=1; i<=p; i++) {       // 산타 수만큼 반복
            if(sleepTimeArr[i] >= turn || isDead[i]) {   // 기절했거나 게임에서 탈락한 산타라면
                continue;
            }
            Position santaPos = santaArr[i];        // 산타 위치
            Position newSantaPos = new Position(santaPos.x, santaPos.y);
            int curDistance = getDistance(deer.x, deer.y, santaPos.x, santaPos.y);    // 현재 위치에서 루돌프와 거리
            int minDistance = curDistance;
            int moveD = -1;     // 움직인 방향
            for(int d=0; d<4; d++) {
                int nx = DX[d] + santaPos.x;
                int ny = DY[d] + santaPos.y;
                if(!isValid(nx, ny) || arr[nx][ny] != 0) {      // 다른 산타가 이미 있는 위치거나 격자 밖이라면
                    continue;
                }
                int newDistance = getDistance(deer.x, deer.y, nx, ny);      // 움직인 위치에서 루돌프와 거리
                if(minDistance > newDistance) {
                    minDistance = newDistance;      // 최소 거리 갱신
                    moveD = d;
                    // 새 위치 갱신
                    newSantaPos.x = nx;
                    newSantaPos.y = ny;
                }
            }
            if(curDistance > minDistance) {      // 산타를 움직일 수 있다면
                move(santaPos.x, santaPos.y, newSantaPos.x, newSantaPos.y);       // 산타 위치 갱신
                if(arr[deer.x][deer.y] != 0) {      // 산타와 루돌프가 충돌했다면
                    collision(DX[(moveD + 2) % 4], DY[(moveD + 2) % 4], d);
                }
            }
        }
    }

    public static boolean isAllDead() {
        for(int i=1; i<=p; i++) {
            if(!isDead[i]) {        // 안 죽은 산타를 찾으면
                return false;
            }
        }
        return true;
    }

    /*
        살아있는 산타는 점수를 1씩 부여
    */
    public static void giveScore() {
        for(int i=1; i<=p; i++) {
            if(!isDead[i]) {
                score[i]++;
            }
        }
    }

    public static void printAnswer() {
        for(int i=1; i<=p; i++) {
            System.out.print(score[i] + " ");
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        p = Integer.parseInt(st.nextToken());
        c = Integer.parseInt(st.nextToken());
        d = Integer.parseInt(st.nextToken());
        arr = new int[n + 1][n + 1];
        isDead = new boolean[p + 1];
        santaArr = new Position[p + 1];
        sleepTimeArr = new int[p + 1];
        score = new int[p + 1];

        // 루돌프 위치 입력받기
        st = new StringTokenizer(br.readLine());
        deer = new Position(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));

        // 산타의 번호와 초기 위치 입력받기
        for(int i=0; i<p; i++) {
            st = new StringTokenizer(br.readLine());
            int p = Integer.parseInt(st.nextToken());       // 산타 번호
            int x = Integer.parseInt(st.nextToken());       // 행
            int y = Integer.parseInt(st.nextToken());       // 열
            arr[x][y] = p;
            santaArr[p] = new Position(x, y);
        }

        for(turn=1; turn<=m; turn++) {
            deerMove();
            santaMove();
            if(isAllDead()) {       // 산타가 모두 죽었으면
                break;
            }
            giveScore();        // 살아있는 산타는 점수를 1 받는다
        }

        // 각 산타의 점수 출력
        printAnswer();
    }
}