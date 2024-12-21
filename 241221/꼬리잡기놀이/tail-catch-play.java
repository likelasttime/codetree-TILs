import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    final static int MAX_N = 20;
    // 우상좌하
    final static int[] DX = {0, -1, 0, 1};
    final static int[] DY = {1, 0, -1, 0};

    static int n;       // 격자의 크기
    static int m;       // 팀의 수
    static int[][] arr;
    static int[][] matrix;
    static boolean[][] visit;
    static List<Position>[] team;
    static int[] tail;      // 각 팀별로 꼬리 인덱스 저장
    static int answer;      // 최종 점수
    static int k;       // 전체 라운드 수

    static class Position {
        int x;
        int y;
        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Node {
        int head;
        int tail;
        Node(int head, int tail) {
            this.head = head;
            this.tail = tail;
        }
    }

    /*
        각 팀은 머리사람을 따라 한 칸 이동
    */
    public static void move(int teamIdx) {
        int len = team[teamIdx].size();
        Position tmp = team[teamIdx].get(len - 1);
        for(int i=len-1; i>=1; i--) {
            team[teamIdx].set(i, team[teamIdx].get(i - 1));
        }
        team[teamIdx].set(0, tmp);
    }

    public static void dfs(Position cur, int idx) {
        if(arr[cur.x][cur.y] == 3) {        // 꼬리
            tail[idx] = team[idx].size() + 1;
        }
        matrix[cur.x][cur.y] = idx;     // (cur.x, cur.y) 위치에 idx번째 팀이 있다.
        team[idx].add(new Position(cur.x, cur.y));
        visit[cur.x][cur.y] = true;
        for(int d=0; d<4; d++) {
            int nx = cur.x + DX[d];
            int ny = cur.y + DY[d];
            // 유효하지 않은 좌표이거나 방문했거나 빈 칸은 건너뛰기
            if(!isValid(nx, ny) || visit[nx][ny] || arr[nx][ny] == 0) {
                continue;
            } else if(team[idx].size() == 1 && arr[nx][ny] != 2) {      // 머리만 찾았다면, 중간 사람들을 넣어줘야 한다.
                continue;
            }
            dfs(new Position(nx, ny), idx);
        }
    }

    /*
        머리와 꼬리를 바꾸기
     */
    public static void reverse(int teamIdx) {
        List<Position> copied = new ArrayList();
        for(int i=tail[teamIdx]-1; i>=0; i--) {
            copied.add(team[teamIdx].get(i));
        }
        for(int i=team[teamIdx].size()-1; i>=tail[teamIdx]; i--) {
            copied.add(team[teamIdx].get(i));
        }
        team[teamIdx] = copied;
    }

    /*
        공을 던져서 사람을 맞춘다
    */
    public static void throwBall(int turn) {
        int curT = (turn - 1) % (4 * n) + 1;
        if(curT <= n) {      // 오른쪽으로 던지기
            for(int y=0; y<n; y++) {            // 열
                if(matrix[curT - 1][y] == -1) {     // 아무런 팀이 없는 곳
                    continue;
                }
                for(int t=0; t<tail[matrix[curT - 1][y]]; t++) {
                    Position cur = team[matrix[curT - 1][y]].get(t);
                    if(cur.x == curT - 1 && cur.y == y) {      // 사람이 공에 맞았다면
                        answer += (t + 1) * (t + 1);
                        reverse(matrix[curT - 1][y]);
                        return;
                    }
                }
            }
        } else if(curT <= 2 * n) {   // 위로 던지기
            curT -= n;
            for(int x=n-1; x>=0; x--) {            // 행
                if(matrix[x][curT - 1] == -1) {     // 아무런 팀이 없는 곳
                    continue;
                }
                for(int t=0; t<tail[matrix[x][curT - 1]]; t++) {
                    Position cur = team[matrix[x][curT - 1]].get(t);
                    if(cur.x == x && cur.y == curT - 1) {      // 사람이 공에 맞았다면
                        answer += (t + 1) * (t + 1);
                        reverse(matrix[x][curT - 1]);
                        return;
                    }
                }
            }
        } else if(curT <= 3 * n) {       // 왼쪽으로 던지기
            curT -= (2 * n);
            for(int y=n-1; y>=0; y--) {            // 열
                if(matrix[n - curT][y] == -1) {     // 아무런 팀이 없는 곳
                    continue;
                }
                for(int t=0; t<tail[matrix[n - curT][y]]; t++) {
                    Position cur = team[matrix[n - curT][y]].get(t);
                    if(cur.x == n - curT && cur.y == y) {      // 사람이 공에 맞았다면
                        answer += (t + 1) * (t + 1);
                        reverse(matrix[n - curT][y]);
                        return;
                    }
                }
            }
        } else {    // 세로로 던지기
            curT -= (3 * n);
            for(int x=0; x<n; x++) {            // 행
                if(matrix[x][n - curT] == -1) {     // 아무런 팀이 없는 곳
                    continue;
                }
                for(int t=0; t<tail[matrix[x][n - curT]]; t++) {
                    Position cur = team[matrix[x][n - curT]].get(t);
                    if(cur.x == x && cur.y == n - curT) {      // 사람이 공에 맞았다면
                        answer += (t + 1) * (t + 1);
                        reverse(matrix[x][n - curT]);
                        return;
                    }
                }
            }
        }
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        n = sc.nextInt();   // 격자의 크기
        m = sc.nextInt();   // 팀의 개수
        k = sc.nextInt();   // 라운드 수
        arr = new int[n][n];
        matrix = new int[MAX_N + 1][MAX_N + 1];
        visit = new boolean[n][n];
        team = new ArrayList[m];
        tail = new int[m];
        // 0: 빈칸, 1:머리, 2:머리도꼬리도아닌사람, 3:꼬리, 4:이동선
        for(int x=0; x<n; x++) {
            for(int y=0; y<n; y++) {
                arr[x][y] = sc.nextInt();
            }
        }

        for(int i=0; i<n; i++) {
            Arrays.fill(matrix[i], -1);
        }

        int teamCnt = 0;
        for(int x=0; x<n; x++) {
            for(int y=0; y<n; y++) {
                // 머리가 아니거나 방문한 곳은 건너뛰기
                if(arr[x][y] != 1 || visit[x][y]) {
                    continue;
                }
                team[teamCnt] = new ArrayList();
                dfs(new Position(x, y), teamCnt);
                teamCnt++;
            }
        }

        for(int curK=1; curK<=k; curK++) {
            // 각 팀은 머리사람을 따라 한 칸 이동
            for(int i=0; i<m; i++) {
                move(i);
            }
            // 공 던지기
            throwBall(curK);
        }

        // 점수의 총합을 출력
        System.out.println(answer);
    }
}