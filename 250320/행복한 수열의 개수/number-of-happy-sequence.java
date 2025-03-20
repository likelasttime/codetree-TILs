import java.util.Scanner;
  
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int answer = 0;     // 행복한 수열의 수
        int n = sc.nextInt();   // 1 <= 격자의 크기 <= 100
        int m = sc.nextInt();   // 1 <= 연속해야 하는 숫자의 수 <= n
        int[][] grid = new int[n][n];
        // 격자에 대한 정보 입력 받기
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                grid[i][j] = sc.nextInt();
        
        // 행 탐색
        for(int row=0; row<n; row++) {
            int prev = grid[row][0];
            int cnt = 1;        
            for(int col=1; col<n; col++) {
                if(prev == grid[row][col]) {
                    cnt++;
                } else {
                    cnt = 1;        // 연속된 수의 갯수 초기화
                }
                if(cnt >= m) {      // 한 행에 m개 이상의 동일한 원소 존재
                    answer++;
                    break;
                }
                prev = grid[row][col];      // 이전 값 갱신
            }
        }

        // 열 탐색
        for(int col=0; col<n; col++) {
            int prev = grid[0][col];    // 상단에 있는 값으로 초기화
            int cnt = 1;        
            for(int row=1; row<n; row++) {
                if(prev == grid[row][col]) {
                    cnt++;
                } else {
                    cnt = 1;        // 연속된 수의 갯수 초기화
                }
                if(cnt >= m) {      // 한 행에 m개 이상의 동일한 원소 존재
                    answer++;
                    break;
                }
                prev = grid[row][col];      // 이전 값 갱신
            }
        }

        System.out.println(answer);       
    }
}