import java.util.Scanner;

public class Main {
    static int k;
    static int n;
    static int[] arr;

    public static void dfs(int depth) {
        if(depth >= n) {
            for(int i=0; i<n; i++) {
                System.out.print(arr[i] + " ");
            }
            System.out.println();
            return;
        }
        for(int i=1; i<=k; i++) {
            arr[depth] = i;
            dfs(depth + 1);
            arr[depth] = 0; 
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        k = sc.nextInt();
        n = sc.nextInt();
        arr = new int[n];

        dfs(0);
    }
}