import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();       // 3 <= 격자의 크기 <= 20
        int[][] grid = new int[n][n];
        int answer = 0;     // 최대 동전의 수
        // 격자 정보 입력 받기
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = sc.nextInt();  // 1: 동전 있음, 0: 동전 없음
            }
        }

        for(int i=0; i<=n-3; i++) {
            for(int j=0; j<=n-3; j++) {
                int x = i;
                int y = j;
                int cnt = 0;
                for(int row=x; row<x+3; row++) {
                    for(int col=y; col<y+3; col++) {
                        if(grid[row][col] == 1) {       // 동전 발견
                            cnt++;
                        }
                    }
                }
                answer = Math.max(answer, cnt);
            }
        }
        
        System.out.println(answer);
    }
}