import java.util.Scanner;
import java.util.Arrays;

public class Main {
    static int answer;      // N자리 아름다운 수의 갯수
    static int n;
    static int[] arr;

    public static void dfs(int depth) {
        if(depth >= n) {
            int[] cntArr = new int[5];
            int prev = arr[0];
            for(int i=0; i<n; i++) {
                if(arr[i] == prev) {
                    cntArr[arr[i]]++;

                } else if(cntArr[prev] % prev == 0) {
                    cntArr[arr[i]] = 1;
                } 
                else {
                    return;
                }
                prev = arr[i];      // 이전 값 갱신
            }
            for(int i=1; i<=4; i++) {
                if(cntArr[i] % i != 0) {
                    return;
                }
            }
            answer++;
            return;
        }
        for(int i=1; i<=4; i++) {
            arr[depth] = i;
            dfs(depth + 1);
            arr[depth] = 0;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        n = sc.nextInt();
        arr = new int[n];

        dfs(0);
        System.out.println(answer);
    }
}