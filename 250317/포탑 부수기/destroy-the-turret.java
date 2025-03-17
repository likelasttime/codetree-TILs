import java.io.*;
import java.util.*;

public class Main {
    // 우하좌상
    final static int[] DX = {0, 1, 0, -1, -1, -1, 1, 1};
    final static int[] DY = {1, 0, -1, 0, -1, 1, -1, 1};

    static int n;       // 4 ~ 10
    static int m;       // 4 ~ 10
    static int k;       // 1 ~ 1,000
    static int[][] arr;
    static boolean[][] isDie;   // 부서진 포탑을 저장하는 배열
    static int[][] lastTime;        // 최근에 공격한 시점을 저장하는 배열
    static boolean[][] isActive;        // 해당 회차에서 공격과 무관한 포탑은 false로 저장
    static int cnt;
    static int[][] distance;

    static class Position {
        int row;
        int col;
        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    /*
        공격자 선정
    */
    public static int[] chooseAttacker() {
        List<int[]> lst = new ArrayList();
        for(int row=0; row<n; row++) {
            for(int col=0; col<m; col++) {
                // 부서진 포탑은 건너뛰기
                if(isDie[row][col]) {
                    continue;
                }
                lst.add(new int[]{row, col, arr[row][col], lastTime[row][col], row + col, col});
            }
        }
        Collections.sort(lst, new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                if(a[2] != b[2]) {
                    return Integer.compare(a[2], b[2]);     // 공격력이 낮은 순
                }
                if(a[3] != b[3]) {
                    return Integer.compare(b[3], a[3]);     // 가장 최근에 공격한 포탑
                }
                if(a[4] != b[4]) {
                    return Integer.compare(b[4], a[4]);       // 행과 열의 합이 큰 순
                }
                return Integer.compare(b[5], a[5]);     // 열이 큰 순
            }
        });
        return lst.get(0);
    }

    /*
        레이저 공격
        attacker = 공격자 좌표
        target = 공격 대상자 좌표
    */
    public static void laser(Position attacker, Position target) {
        Queue<Position> que = new ArrayDeque();
        boolean[][] visit = new boolean[n][m];
        que.offer(target);
        visit[target.row][target.col] = true;
        distance = new int[n][m];
        for(int row=0; row<n; row++) {
            Arrays.fill(distance[row], n * m);
        }
        distance[target.row][target.col] = 0;
        while(!que.isEmpty()) {
            Position cur = que.poll();
            for(int d=0; d<4; d++) {
                int nx = (DX[d] + cur.row + n) % n;
                int ny = (DY[d] + cur.col + m) % m;
                if(visit[nx][ny] || isDie[nx][ny]) {      // 이미 방문했거나 부서진 포탑이 있는 위치
                    continue;
                }
                visit[nx][ny] = true;
                distance[nx][ny] = distance[cur.row][cur.col] + 1;
                que.offer(new Position(nx, ny));
            }
        }
    }

    /*
        공격자의 공격
        x, y = 공격자의 좌표
    */
    public static void attack(int x, int y) {
        List<int[]> lst = new ArrayList();
        for(int row=0; row<n; row++) {
            for(int col=0; col<m; col++) {
                // 부서진 포탑 또는 공격자 자신은 건너뛰기
                if(isDie[row][col] || (row == x && col == y)) {
                    continue;
                }
                lst.add(new int[]{row, col, arr[row][col], lastTime[row][col], row + col, col});
            }
        }
        Collections.sort(lst, new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                if(a[2] != b[2]) {
                    return Integer.compare(b[2], a[2]);     // 공격력이 높은 순
                }
                if(a[3] != b[3]) {
                    return Integer.compare(a[3], b[3]);     // 가장 오래 전에 공격한 포탑
                }
                if(a[4] != b[4]) {
                    return Integer.compare(a[4], b[4]);       // 행과 열의 합이 작은 순
                }
                return Integer.compare(a[5], b[5]);     // 열이 작은 순
            }
        });

        int[] target = lst.get(0);      // 공격 대상자

        // 레이저 공격
        Position attackerPos = new Position(x, y);
        Position targetPos = new Position(target[0], target[1]);
        laser(attackerPos, targetPos);
        if(distance[attackerPos.row][attackerPos.col] < n * m) {
            decreasePower(attackerPos, targetPos);
        } else {
            // 포탄 공격
            photon(attackerPos, targetPos);
        }
    }

    public static void photon(Position attacker, Position target) {
        // 공격 대상은 공격자의 공격력 만큼 피해를 입는다
        arr[target.row][target.col] -= arr[attacker.row][attacker.col];
        isActive[target.row][target.col] = true;
        // 주위 8개의 방향에 있는 포탑도 피해를 입는다
        for(int d=0; d<8; d++) {
            int nx = (n + target.row + DX[d]) % n;
            int ny = (m + target.col + DY[d]) % m;
            if(attacker.row == nx && attacker.col == ny) {      // 공격자는 영향을 받지 않는다
                continue;
            }
            arr[nx][ny] -= arr[attacker.row][attacker.col] / 2;
            isActive[nx][ny] = true;
        }
    }

    public static void decreasePower(Position attacker, Position target) {
        // 공격 대상은 공격자의 공격력 만큼 피해를 입는다
        arr[target.row][target.col] -= arr[attacker.row][attacker.col];
        isActive[target.row][target.col] = true;
        // 공격자 공격력의 절반 만큼 피해를 입는다
        int x = attacker.row;
        int y = attacker.col;
        while(x != target.row && y != target.col) {
            for(int d=0; d<4; d++) {
                int nx = (n + DX[d] + x) % n;
                int ny = (m + DY[d] + y) % m;
                if (nx == target.row && ny == target.col) {
                    break;
                }
                if (distance[nx][ny] < distance[x][y]) {
                    arr[nx][ny] -= arr[attacker.row][attacker.col] / 2;
                    isActive[nx][ny] = true;
                    x = nx;
                    y = ny;
                    break;
                }
            }
        }
    }

    /*
        공격력이 0이하인 포탑은 부서짐
    */
    public static void die() {
        for(int row=0; row<n; row++) {
            for(int col=0; col<m; col++) {
                if(isDie[row][col]) {       // 이미 부서진 포탑은 건너뛰기
                   continue;
                }
                if(arr[row][col] <= 0) {
                    isDie[row][col] = true;
                    cnt--;
                }
            }
        }
    }

    /*
        포탑 정비
     */
    public static void upgrade() {
        for(int row=0; row<n; row++) {
            for(int col=0; col<m; col++) {
                if(isDie[row][col] || isActive[row][col]) {       // 부서진 포탑이거나 공격과 무관하지 않은 포탑
                    continue;
                }
                arr[row][col]++;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        k = Integer.parseInt(st.nextToken());
        cnt = n * m;
        arr = new int[n][m];
        isDie = new boolean[n][m];
        lastTime = new int[n][m];
        isActive = new boolean[n][m];
        for(int row=0; row<n; row++) {
            st = new StringTokenizer(br.readLine());
            for(int col=0; col<m; col++) {
                arr[row][col] = Integer.parseInt(st.nextToken());
            }
        }

        die();        // 초반에 공격력이 0인 포탑은 부서진 포탑

        while(k-- > 0) {
            // 공격자 선정
            isActive = new boolean[n][m];
            int[] attacker = chooseAttacker();
            lastTime[attacker[0]][attacker[1]] = k;
            isActive[attacker[0]][attacker[1]] = true;
            arr[attacker[0]][attacker[1]] += (n + m);       // 공격력 증가

            // 공격자의 공격
            attack(attacker[0], attacker[1]);

            // 포탑 부서짐
            die();

            // 포탑 정비
            upgrade();

            if(cnt <= 1) {
                break;
            }
        }

        int answer = 0;     // 최대 공격력
        for(int row=0; row<n; row++) {
            for(int col=0; col<m; col++) {
                if(isDie[row][col]) {       // 부서진 포탑이라면
                    continue;
                }
                answer = Math.max(answer, arr[row][col]);
            }
        }
        System.out.print(answer);
    }
}