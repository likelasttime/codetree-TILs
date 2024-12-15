import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    // 우상좌하
    final static int[] DX = {0, -1, 0, 1};
    final static int[] DY = {1, 0, -1, 0};

    static int n;       // 격자의 크기
    static int m;       // 팀의 수
    static int[][] arr;
    static boolean[][] visit;
    static List<Position>[] team;
    static Node[] node;     // 각 팀별로 머리와 꼬리 인덱스 저장
    static int lineIdx;     // 공을 던질 인덱스
    static int linePos;     // 공을 던질 방향
    static int answer;      // 최종 점수
    static boolean[] goRight;       // 오른쪽으로 가는지

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
        Node info = node[teamIdx];
        int len = team[teamIdx].size();
        if(!goRight[teamIdx]) {     // 왼쪽으로 이동
            info.head = (info.head - 1 + len) % len;
            info.tail = (info.tail - 1 + len) % len;
        } else {        // 오른쪽으로 이동
            info.head = (info.head + 1) % len;
            info.tail = (info.tail + 1) % len;
        }
    }

    public static void dfs(Position cur, int idx) {
        if(arr[cur.x][cur.y] == 1) {        // 머리
            node[idx].head = team[idx].size();
        }
        if(arr[cur.x][cur.y] == 3) {        // 꼬리
            node[idx].tail = team[idx].size();
        }
        team[idx].add(new Position(cur.x, cur.y));
        visit[cur.x][cur.y] = true;
        for(int d=0; d<4; d++) {
            int nx = cur.x + DX[d];
            int ny = cur.y + DY[d];
            // 유효하지 않은 좌표이거나 방문했거나 빈 칸은 건너뛰기
            if(!isValid(nx, ny) || visit[nx][ny] || arr[nx][ny] == 0) {
                continue;
            }
            dfs(new Position(nx, ny), idx);
        }
    }

    /*
        공을 던져서 사람을 맞춘다
    */
    public static void throwBall() {
        if(linePos == 0 || linePos == 2) {      // 가로로 던지기
            for(int y=0; y<n; y++) {            // 열
                for(int t=0; t<m; t++) {        // 팀 인덱스
                    int start = Math.min(node[t].tail, node[t].head);
                    int end = Math.max(node[t].tail, node[t].head);
                    if(node[t].tail < node[t].head) {
                        end = (n + node[t].tail - 1) % team[t].size();
                    }
                    for(int i=start; i<=end; i++) {
                        if(lineIdx == team[t].get(i).x && y == team[t].get(i).y) {    // 공에 맞았다면
                            // 점수 부여
                            int distance = Math.abs(node[t].head - i) + 1;
                            answer += (distance * distance);
                            // 머리와 꼬리가 바뀐다
                            int tmp = node[t].tail;
                            node[t].tail = node[t].head;
                            node[t].head = tmp;
                            goRight[t] = !goRight[t];
                            return;
                        }
                    }
                }
            }
        } else {    // 세로로 던지기
            for(int x=0; x<n; x++) {            // 행
                for(int t=0; t<m; t++) {        // 팀 인덱스
                    int start = Math.min(node[t].tail, node[t].head);
                    int end = Math.max(node[t].tail, node[t].head);
                    if(node[t].tail < node[t].head) {
                        end = (n + node[t].tail - 1) % team[t].size();
                    }
                    for(int i=start; i<=end; i++) {
                        if(lineIdx == team[t].get(i).y && x == team[t].get(i).x) {    // 공에 맞았다면
                            // 점수 부여
                            int distance = Math.abs(node[t].head - i) + 1;
                            answer += (distance * distance);
                            // 머리와 꼬리가 바뀐다
                            int tmp = node[t].tail;
                            node[t].tail = node[t].head;
                            node[t].head = tmp;
                            goRight[t] = !goRight[t];
                            return;
                        }
                    }
                }
            }
        }
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    /*
        공을 던질 선을 구하기
    */
    public static void chooseLine() {
        if(lineIdx + 1 == n) {
            lineIdx = 0;
            linePos = (linePos + 1) % 4;
        } else {
            lineIdx++;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        n = sc.nextInt();   // 격자의 크기
        m = sc.nextInt();   // 팀의 개수
        int k = sc.nextInt();   // 라운드 수
        arr = new int[n][n];
        visit = new boolean[n][n];
        team = new ArrayList[m];
        node = new Node[m];
        goRight = new boolean[m];
        lineIdx = -1;
        Arrays.fill(goRight, true);
        // 0: 빈칸, 1:머리, 2:머리도꼬리도아닌사람, 3:꼬리, 4:이동선
        for(int x=0; x<n; x++) {
            for(int y=0; y<n; y++) {
                arr[x][y] = sc.nextInt();
            }
        }
        int teamCnt = 0;
        for(int x=0; x<n; x++) {
            for(int y=0; y<n; y++) {
                // 빈 칸 또는 방문한 곳은 건너뛰기
                if(arr[x][y] == 0 || visit[x][y]) {
                    continue;
                }
                team[teamCnt] = new ArrayList();
                node[teamCnt] = new Node(-1, -1);
                dfs(new Position(x, y), teamCnt);
                teamCnt++;
            }
        }

        while(k-- > 0) {
            // 각 팀은 머리사람을 따라 한 칸 이동
            for(int i=0; i<m; i++) {
                move(i);
            }
            // 공을 던질 방향, 칸 구하기
            chooseLine();
            // 공 던지기
            throwBall();
        }

        // 점수의 총합을 출력
        System.out.println(answer);
    }
}