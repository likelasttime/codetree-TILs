import java.io.*;
import java.util.StringTokenizer;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Main {
    static class TravelPackage implements Comparable<TravelPackage> {
        int id;     // 고유한 식별자
        int revenue;    // 매출
        int dest;       // 도착지
        int profit;   // 이득(매출 - 최단 거리)

        TravelPackage(int id, int revenue, int dest, int profit) {
            this.id = id;
            this.revenue = revenue;
            this.dest = dest;
            this.profit = profit;
        }

        @Override
        public int compareTo(TravelPackage o) {
            if(this.profit == o.profit) {       // 이익이 같다면
                return Integer.compare(this.id, o.id);      // id가 작은 순 
            } 
            return Integer.compare(o.profit, this.profit);      // 이득이 높은 순
        }
    }

    final static int MAX_ID = 30000;

    static StringTokenizer st;
    static int n;       // 정점의 갯수
    static int[][] arr;     // 인접 행렬
    static int[] dijkstraArr;     // 시작 도시부터 모든 도시 각각의 최단 경로 저장
    static int startCity = 0;       // 초기에 출발 도시는 0번 도시  
    static boolean[] isCancel;      // 이 도시가 취소된 여행상품인지
    static boolean[] isCreated;     // 이 도시의 여행상품이 있는지
    static PriorityQueue<TravelPackage> travelPackages = new PriorityQueue();   // 이익이 높은 순으로 여행 상품을 추천하기 위해

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        int q = Integer.parseInt(br.readLine());    // 명령의 수
        
        // 명령의 정보 입력 받기
        for(int i=0; i<q; i++) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());     // 명령어
            
            if(cmd == 100) {        // 코드트리 랜드 건설
                n = Integer.parseInt(st.nextToken());     // 정점의 갯수
                int m = Integer.parseInt(st.nextToken());     // 간선의 갯수
                
                init(n, m);     // 인접행렬 초기화
                dijkstra();     // 출발점 0번에서 각 도시로의 최단 거리 계산
            } else if(cmd == 200) {     // 여행 상품 생성
                int id = Integer.parseInt(st.nextToken());      // 고유 식별자
                int revenue = Integer.parseInt(st.nextToken());     // 매출
                int dest = Integer.parseInt(st.nextToken());    // 도착지
               
                createPackage(id, revenue, dest);       // 우선순위 큐에 여행 상품 추가
            } else if(cmd == 300) {     // 여행 상품 취소
                int id = Integer.parseInt(st.nextToken());
                
                cancelPackage(id);
            } else if(cmd == 400) {     // 최적의 여행 상품 판매
                bw.write(String.valueOf(sellPackage()) + "\n");
            } else {        // 여행 상품의 출발지 변경
                startCity = Integer.parseInt(st.nextToken());       // 새로운 출발지
                
                dijkstra();     // 새로운 출발지에서 각 도시까지의 최단 거리 계산
                updatePackage();    // 이익을 다시 계산해서 우선순위 큐 업데이트
            }
        }

        bw.flush();
    }

    public static void cancelPackage(int id) {
        if(isCreated[id]) {    // 여행 상품이 있으면
            isCancel[id] = true;
        }
    }

    public static int sellPackage() {
        while(!travelPackages.isEmpty()) {
            TravelPackage travelPackage = travelPackages.peek();
                    
            if(travelPackage.profit < 0) {      // 이득이 음수라면 더이상 볼 필요없음
                break;
            }

            travelPackages.poll();      // 우선순위가 높은 여행 패키지를 꺼내기

            if(!isCancel[travelPackage.id]) {       // 취소된 여행 상품이 아니라면
                return travelPackage.id;
            }
        }
        return -1;      // 조건에 맞는 여행 패키지가 없음
    }

    public static void updatePackage() {
        List<TravelPackage> packages = new ArrayList();

        // 기존 여행 상품들의 이익을 갱신해야 하니까 우선순위 큐 비우기
        while(!travelPackages.isEmpty()) {
            packages.add(travelPackages.poll());
        }

        // 이익을 다시 계산해서 우선순위 큐에 넣기
        for(TravelPackage tp : packages) {
            createPackage(tp.id, tp.revenue, tp.dest);
        }
    }

    public static void createPackage(int id, int revenue, int dest) {
        int profit = revenue - dijkstraArr[dest];
        isCreated[id] = true;   // id번째 도시의 여행 상품이 만들어졌다.

        travelPackages.add(new TravelPackage(id, revenue, dest, profit));       // 우선순위큐에 추가
    }

    public static void dijkstra() {
        boolean[] visit = new boolean[n];
        Arrays.fill(dijkstraArr, Integer.MAX_VALUE);    // 초기화
        dijkstraArr[startCity] = 0;     // 출발 도시 초기화

        for(int i=0; i<n-1; i++) {
            int v = 0;
            int minDistance = Integer.MAX_VALUE;

            // 방문하지 않은 노드 중 가장 비용이 적은 노드를 찾기
            for(int j=0; j<n; j++) {
                if(!visit[j] && minDistance > dijkstraArr[j]) {     // j를 방문하지 않았고, 비용이 적게 든다면
                    v = j;
                    minDistance = dijkstraArr[j];
                }
            }

            visit[v] = true;    // v 도시 방문 처리

            // 정점 v -> 정점 j까지의 최단 거리 계산한다. 이것은 결국 시작 정점 -> 정점 v -> 정점 j
            for(int j=0; j<n; j++) {
                // j에 방문하지 않았고, v에서 j로 갈 수 있고, 최단 거리를 갱신해야 한다면
                if(!visit[j] && dijkstraArr[v] != Integer.MAX_VALUE && arr[v][j] != Integer.MAX_VALUE && dijkstraArr[j] > dijkstraArr[v] + arr[v][j]) {
                    dijkstraArr[j] = dijkstraArr[v] + arr[v][j];        // (시작 정점 -> v로의 최단 거리) + (v -> j까지의 가중치)
                }
            }
        }
    }

    public static void init(int n, int m) {
        arr = new int[n + 1][n + 1];
        dijkstraArr = new int[n + 1];
        isCancel = new boolean[MAX_ID];
        isCreated = new boolean[MAX_ID];

        // 인접 행렬 초기화
        for(int x=0; x<n; x++) {
            Arrays.fill(arr[x], Integer.MAX_VALUE);     // 초기화
            arr[x][x] = 0;      // 출발지와 도착지가 같으면 0
        }

        for(int j=0; j<m; j++) {
            int a = Integer.parseInt(st.nextToken());   // 도시 1
            int b = Integer.parseInt(st.nextToken());   // 도시 2
            int w = Integer.parseInt(st.nextToken());   // 가중치

            // 무방향 연결(두 도시 사이를 연결하는 간선은 여러 개가 존재할 수 있다)
            arr[a][b] = Math.min(arr[a][b], w);
            arr[b][a] = Math.min(arr[b][a], w);
        }
    }
}