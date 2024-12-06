import java.io.*;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
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

    static class Direction extends Position {
        int d;

        Direction(int x, int y, int d) {
            super(x, y);
            this.d = d;
        }
    }

    static int n;       // 격자 크기
    static int m;       // 도망자의 수
    static int h;       // 나무의 수
    static int k;       // 턴 수
    static int[] direction;   // 도망자의 이동 방향(상하 또는 좌우)
    static int[] dirDetail;     // 상하 타입이면 상 또는 하, 좌우 타입이면 좌 또는 우
    static Position[] pos;  // 도망자의 위치
    static int[][] runnerArr;       // 위치에 도망자의 인덱스를 저장
    static int[][] arr;
    static int answer;      // 술래의 총 점수
    static Position monster;   // 술래
    static List<Direction> snail;    // 술래가 나선형으로 움직였을 때 위치 좌표들
    static int snailIdx;
    static boolean isGo;        // 중앙에서 출발했는지
    // 센터 좌표
    static int CENTER_X;
    static int CENTER_Y;

    // 상우하좌
    final static int[] DX = {-1, 0, 1, 0};
    final static int[] DY = {0, 1, 0, -1};
    // 상하
    final static int[] TOP_OR_BOTTOM_DX = {-1, 1};
    final static int[] TOP_OR_BOTTOM_DY = {0, 0};
    // 좌우
    final static int[] LEFT_OR_RIGHT_DX = {0, 0};
    final static int[] LEFT_OR_RIGHT_DY = {-1, 1};

    public static int manhattanDistance(int idx) {
        return Math.abs(monster.x - pos[idx].x) + Math.abs(monster.y - pos[idx].y);
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    public static int[] getNewPosition(int i) {
        int nx;
        int ny;
        if(direction[i] == 1) {     // 좌우 타입
            nx = pos[i].x + LEFT_OR_RIGHT_DX[dirDetail[i]];
            ny = pos[i].y + LEFT_OR_RIGHT_DY[dirDetail[i]];
        } else {        // 상하 타입
            nx = pos[i].x + TOP_OR_BOTTOM_DX[dirDetail[i]];
            ny = pos[i].y + TOP_OR_BOTTOM_DY[dirDetail[i]];
        }
        return new int[]{nx, ny};
    }

    public static void initSnail() {
        int curX = monster.x;
        int curY = monster.y;
        int d = 1;  // 이동 거리
        int idx = 0;
        int cnt = 0;
        boolean flag = true;
        snail.add(new Direction(curX, curY, idx));

        while(flag) {
            for(int i=0; i<d; i++) {
                curX += DX[idx];
                curY += DY[idx];
                if(curX < 0 || curY < 0) {
                    flag = false;
                    break;
                }
                snail.add(new Direction(curX, curY, idx));
            }
            cnt++;
            idx = (idx + 1) % 4; // 방향 변경
            snail.get(snail.size() - 1).d = idx;
            if(cnt == 2) {
                d++;        // 이동 거리 증가
                cnt = 0;
            }
        }
    }

    /*
        술래가 움직인다
    */
    public static void monsterMove() {
        // 끝에 도달했다면
        if(monster.x == 0 && monster.y == 0) {
            // 다시 거꾸로 중심으로 이동
            isGo = false;
        } else if(monster.x == CENTER_X && monster.y == CENTER_Y) {   // 중앙에 도달하면
            // 끝을 향해 이동
            isGo = true;
        }

        // 끝을 향해 이동
        if(isGo) {
            snailIdx++;
        } else {    // 중앙을 향해 이동
            snailIdx--;
        }

        Direction tmp = snail.get(snailIdx);
        monster = new Position(tmp.x, tmp.y);
    }

    /*
        술래에게 잡혀서 도망자가 죽는다
     */
    public static boolean kill(int nx, int ny) {
        // 유효하지 않은 위치이거나 나무가 있다면
        if(!isValid(nx, ny) || arr[nx][ny] == 1) {
            return false;
        }
        // 도망자를 찾았다면
        if(runnerArr[nx][ny] != -1) {
            int killIdx = runnerArr[nx][ny];
            runnerArr[nx][ny] = -1;
            pos[killIdx].x = -1;
            pos[killIdx].y = -1;
            return true;
        }
        return false;
    }

    /*
        술래가 도망자를 잡는다
    */
    public static int gotcha() {
        int cnt = 0;        // 잡은 도망자 수
        // 현재 있는 칸을 포함하여 3칸을 내다보기
        int curD = snail.get(snailIdx).d;       // 현재 바라보는 방향
        int curX = monster.x;
        int curY = monster.y;

        if(kill(curX, curY)) {
            cnt++;
        }

        for(int i=0; i<2; i++) {
            int nx = DX[curD] + curX;
            int ny = DY[curD] + curY;
            if(kill(nx, ny)) {
                cnt++;
            }
            curX = nx;
            curY = ny;
        }
        return cnt;
    }

    /*
        도망자가 움직인다
    */
    public static void run() {
        for(int i=0; i<m; i++) {
            if(pos[i].x == -1) {      // 잡혔던 도망자는 건너뛰기
                continue;
            }
            // 움직이지 않아도 되는 도망자라면
            if(manhattanDistance(i) > 3) {
                continue;
            }
            // 움직이는 타입 구별
            int[] newPos = getNewPosition(i);
            int nx = newPos[0];
            int ny = newPos[1];

            // 격자를 벗어나지 않는 경우
            if(isValid(nx, ny)) {
                // 움직이려는 칸에 술래가 있다면
                if(monster.x == nx && monster.y == ny) {
                    continue;
                }
                runnerArr[pos[i].x][pos[i].y] = -1;
                pos[i].x = nx;
                pos[i].y = ny;
                runnerArr[nx][ny] = i;
            } else {        // 격자를 벗어난 경우
                // 반대 방향으로 튼다
                dirDetail[i] = (dirDetail[i] + 1) % 2;
                newPos = getNewPosition(i);
                nx = newPos[0];
                ny = newPos[1];
                // 술래가 있으면
                if(monster.x == nx && monster.y == ny) {
                    continue;
                }
                runnerArr[pos[i].x][pos[i].y] = -1;
                pos[i].x = nx;
                pos[i].y = ny;
                runnerArr[nx][ny] = i;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        h = Integer.parseInt(st.nextToken());
        k = Integer.parseInt(st.nextToken());
        CENTER_X = n / 2;
        CENTER_Y = n / 2;
        monster = new Position(CENTER_X, CENTER_Y);   // 정가운데 위치
        snail = new ArrayList();

        // 도망자의 위치와 이동 방법을 입력 받기
        direction = new int[m];
        dirDetail = new int[m];
        pos = new Position[m];
        runnerArr = new int[n][n];
        for(int i=0; i<n; i++) {
            Arrays.fill(runnerArr[i], -1);
        }
        for(int i=0; i<m; i++) {
            st = new StringTokenizer(br.readLine());
            int x = Integer.parseInt(st.nextToken()) - 1;
            int y = Integer.parseInt(st.nextToken()) - 1;
            pos[i] = new Position(x, y);
            direction[i] = Integer.parseInt(st.nextToken());
            runnerArr[x][y] = i;        // 위치에 도망자의 인덱스 저장
            if(direction[i] == 1) {     // 좌우 티입
                dirDetail[i] = 1;        // 오른쪽으로 초기화
            } else {
                dirDetail[i] = 1;        // 아래쪽으로 초기화
            }
        }

        // 나무의 위치 입력 받기
        arr = new int[n][n];
        for(int i=0; i<h; i++) {
            st = new StringTokenizer(br.readLine());
            arr[Integer.parseInt(st.nextToken()) - 1][Integer.parseInt(st.nextToken()) - 1] = 1;
        }

        initSnail();
        isGo = true;        // 중앙에서 출발한다

        for(int turn=1; turn<=k; turn++) {
            // 도망자가 움직인다
            run();
            // 몬스터가 움직인다
            monsterMove();
            // 도망자를 잡는다
            int cnt = gotcha();
            answer += turn * cnt;
        }

        System.out.println(answer);
    }
}