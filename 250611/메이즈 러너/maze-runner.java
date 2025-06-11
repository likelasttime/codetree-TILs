import java.util.*;
import java.io.*;

public class Main {
    // 상하좌우
    final static int[] DX = {-1, 1, 0, 0};
    final static int[] DY = {0, 0, -1, 1};

    static int n;       // 4 <= 미로 크기 <= 10
    static int m;       // 1 <= 참가자의 수 <= 10
    static int k;       // 1 <= 게임 턴 수 <= 100
    static int[][] arr;     // 미로(0: 빈 칸, 1 ~ 9: 벽의 내구도)
    static Position exit;       // 출구
    static Position[] personArr;        // 참가자의 위치를 저장하는 배열
    static int totalDistance;       // 모든 참가자의 이동 거리 합

    static class Position {
        int r;      // 행
        int c;      // 열

        Position(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    /*
        정사각형 범위 내에 (r, c)가 들어가면 true 반환
     */
    public static boolean isContain(int sx, int sy, int ex, int ey, int r, int c) {
        if(sx <= r && sy <= c && ex >= r && ey >= c) {
            return true;
        }
        return false;
    }

    /*
        1명 이상의 참가자와 출구를 포함한 가장 작은 정사각형 잡기
        좌상단 r이 작은 순 > c가 작은 순
        int 배열에 좌측 상단 행, 좌측 상단 열, 우측 하단 행, 우측 하단 열을 순서대로 담아서 반환
     */
    public static int[] selectSquare() {
        for(int size=2; size<n; size++) {       // 정사각형 크기
            for(int r=1; r<=n; r++) {       // 좌측 상단 행
                for(int c=1; c<=n; c++) {   // 좌측 상단 열
                    int endR = r + size - 1;    // 우측 하단 행
                    int endC = c + size - 1;    // 우측 하단 열
                    // 정사각형이 격자를 벗어나면
                    if(!isValid(endR, endC)) {
                        continue;
                    }
                    // 탈출구가 정사각형 범위 내에 있다면
                    if(isContain(r, c, endR, endC, exit.r, exit.c)) {
                        for(int num=1; num<=m; num++) {     // 참가자 번호
                            Position pos = personArr[num];      // 위치
                            if(isContain(r, c, endR, endC, pos.r, pos.c)) {     // 정사각형 범위 내에 사람이 있으면
                                return new int[]{r, c, endR, endC};
                            }
                        }
                    }
                }
            }
        }
        return new int[]{};
    }

    /*
        정사각형 범위 내에 있는 사람들의 번호를 반환
     */
    public static List<Integer> getPerson(int sr, int sc, int er, int ec) {
        List<Integer> personLst = new ArrayList();
        for(int i=1; i<=m; i++) {       // 사람 번호
            if(isContain(sr, sc, er, ec, personArr[i].r, personArr[i].c)) {     // 정사각형 범위 내에 있는 사람이면
                personLst.add(i);
            }
        }
        return personLst;
    }

    public static int[] changePos(int sr, int sc, int er, int ec, int r, int c) {
        int[] result = new int[2];
        result[0] = er - (ec - c);
        //result[0] = er - (c - sc);
        //result[1] = sc + (r - sr);
        result[1] = sc + (er - r);
        return result;
    }

    /*
        선택한 정사각형을 시계방향으로 90도 회전 및 내구도 1 감소
     */
    public static void rotate() {
        int[] squarePos = selectSquare();   // 탈출구, 사람을 포함하는 가장 작은 정사각형 좌표
        int sr = squarePos[0];      // 좌측 상단 행
        int sc = squarePos[1];      // 좌측 상단 열
        int er = squarePos[2];      // 우측 하단 행
        int ec = squarePos[3];      // 우측 하단 열
        int y = sc;
        int[][] copied = new int[n + 1][n + 1];
        for(int i=1; i<=n; i++) {
            copied[i] = arr[i].clone();     // 깊은 복사
        }

        for(int r=sr; r<=er; r++) {     // 행
            int x = er;
            for(int c=sc; c<=ec; c++) {     // 열
                arr[r][c] = Math.max(0, copied[x--][y] - 1);     // 내구도 감소
            }
            y++;
        }

        // 정사각형 범위 내에 있는 사람의 위치 갱신
        List<Integer> personLst = getPerson(sr, sc, er, ec);
        for(int num : personLst) {          // 사람 번호
            Position cur = personArr[num];      // 위치
            int[] newPos = changePos(sr, sc, er, ec, cur.r, cur.c);     // 90도 회전된 위치
            personArr[num].r = newPos[0];
            personArr[num].c = newPos[1];
        }

        // 탈출구 위치 갱신
        int[] newPos = changePos(sr, sc, er, ec, exit.r, exit.c);       // 90도 회전된 위치
        exit.r = newPos[0];
        exit.c = newPos[1];
    }

    /*
        (x, y)가 격자내에 있으면 true 반환
    */
    public static boolean isValid(int x, int y) {
        return 1 <= x && x <= n && 1 <= y && y <= n;
    }

    /*
        두 위치간의 최단 거리 반환
    */
    public static int getDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /*
        모든 참가자 m명이 1칸씩 출구와 가까워지는 곳으로 움직이기

    */
    public static void moveAll() {
        for(int i=1; i<=m; i++) {       // 참가자 번호
            Position pos = personArr[i];    // 현재 좌표
            int prevDistance = getDistance(pos.r, pos.c, exit.r, exit.c);   // 원위치와 출구까지의 거리
            int minDistance = prevDistance;      // 최소 거리
            Position nextPos = new Position(pos.r, pos.c);
            for(int d=0; d<4; d++) {
                int nx = DX[d] + pos.r;
                int ny = DY[d] + pos.c;
                // 격자를 벗어나거나 빈칸이 아니면
                if(!isValid(nx, ny) || arr[nx][ny] != 0) {
                    continue;
                }
                int nextDistance = getDistance(nx, ny, exit.r, exit.c);     // 새위치와 출구까지의 거리
                if(minDistance > nextDistance) {
                    minDistance = nextDistance;
                    nextPos.r = nx;
                    nextPos.c = ny;
                }
            }
            // 4방 탐색이 끝난 후 (원위치 ~ 출구 거리) > (새위치 ~ 출구 거리)
            if(prevDistance > minDistance) {
                if(exit.r == nextPos.r && exit.c == nextPos.c) {        // 탈출구에 도착
                    // 격자밖으로 지정
                    personArr[i].r = n + 1;
                    personArr[i].c = n + 1;
                } else {
                    // 위치 갱신
                    personArr[i].r = nextPos.r;
                    personArr[i].c = nextPos.c;
                }
                // 참가자의 이동 거리 합 증가
                totalDistance++;
            }
        }
    }

    /*
        모두 탈출에 성공했으면 true 반환
     */
    public static boolean isEscape() {
        for(int i=1; i<=m; i++) {       // 참가자 번호
            if(personArr[i].r != n + 1) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        k = Integer.parseInt(st.nextToken());
        totalDistance = 0;

        // 미로 입력받기
        arr = new int[n + 1][n + 1];
        for(int x=1; x<=n; x++) {
            st = new StringTokenizer(br.readLine());
            for(int y=1; y<=n; y++) {
                arr[x][y] = Integer.parseInt(st.nextToken());
            }
        }

        // 참가자 좌표 입력받기
        personArr = new Position[m + 1];
        for(int i=1; i<=m; i++) {
            st = new StringTokenizer(br.readLine());
            int r = Integer.parseInt(st.nextToken());       // 행
            int c = Integer.parseInt(st.nextToken());       // 열
            personArr[i] = new Position(r, c);
        }

        // 출구 좌표 입력받기
        st = new StringTokenizer(br.readLine());
        exit = new Position(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));

        for(int turn=1; turn<=k; turn++) {
            moveAll();
            if(isEscape()) {        // 모든 참가자가 탈출에 성공하면
                break;
            }
            rotate();
        }

        System.out.println(totalDistance);        // 모든 참가자의 총 이동거리
        System.out.println(exit.r + " " + exit.c);      // 탈출구 좌표
    }
}