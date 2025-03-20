import java.util.Scanner;

public class Main {
    static int n;       // 3 <= 세로 크기 <= 200
    static int m;        // 3 <= 가로 크기 <= 200
    static int answer;  // 블럭 안에 적힌 숫자합의 최대값
    static int[][] grid;
    static boolean[][] visit;

    final static int[] DX = {-1, 1, 0, 0};
    final static int[] DY = {0, 0, -1, 1};

    static class Position {
        int x;
        int y;
        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < m;
    }

    public static void dfs(Position cur, int sum, int depth) {
        if(depth == 3) {
            answer = Math.max(answer, sum);
            return;
        }
        for(int d=0; d<4; d++) {
            int nx = DX[d] + cur.x;
            int ny = DY[d] + cur.y;
            if(isValid(nx, ny) && !visit[nx][ny]) {
                visit[nx][ny] = true;
                dfs(new Position(nx, ny), sum + grid[nx][ny], depth + 1);
                visit[nx][ny] = false;
            }
        }
        
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);    
        n = sc.nextInt();   // 3 <= 세로 크기 <= 200
        m = sc.nextInt();   // 3 <= 가로 크기 <= 200
        visit = new boolean[n][m];
        grid = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                grid[i][j] = sc.nextInt();      // 1 <= 주어지는 수 <= 1,000
            }
        }
        for(int row=0; row<n; row++) {
            for(int col=0; col<m; col++) {
                visit[row][col] = true;
                dfs(new Position(row, col), 0, 0);
                visit[row][col] = false;
            }
        }
        System.out.println(answer);
    }
}