import java.util.*;

public class Main {
    static int answer;
    static List<Position> bomb;
    static int n;
    static int[][][] dir = {{{-1, 0}, {-2, 0}, {1, 0}, {2, 0}},
            {{-1, 0}, {1, 0}, {0, -1}, {0, 1}},
            {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}
    };

    static class Position {
        int x;
        int y;
        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    public static int[][] copied(int[][] arr) {
        int[][] copied = new int[n][n];
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                copied[i][j] = arr[i][j];
            }
        }
        return copied;
    }

    /*
        depth: 폭탄 설치 갯수
    */
    public static void dfs(int depth, int[][] arr) {
        if(depth == bomb.size()) {
            int cnt = 0;
            for(int x=0; x<n; x++) {
                for(int y=0; y<n; y++) {
                    if(arr[x][y] == 1) {
                        cnt++;
                    }
                }
            }
            answer = Math.max(answer, cnt);
            return;
        }
        for(int i=0; i<3; i++) {
            int[][] newArr = copied(arr);       // 깊은 복사
            Position bombPos = bomb.get(depth);     // 폭탄 위치
            for(int d=0; d<4; d++) {
                int x = bombPos.x + dir[i][d][0];
                int y = bombPos.y + dir[i][d][1];
                if(!isValid(x, y)) {        // 유효하지 않은 좌표라면
                    continue;
                }
                newArr[x][y] = 1;
            }
            dfs(depth + 1, newArr);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        n = sc.nextInt();
        int[][] grid = new int[n][n];
        bomb = new ArrayList();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = sc.nextInt();
                if(grid[i][j] == 1) {       // 폭탄이 있으면
                    bomb.add(new Position(i, j));
                }
            }
        }

        dfs(0, grid);
        System.out.print(answer);
    }
}