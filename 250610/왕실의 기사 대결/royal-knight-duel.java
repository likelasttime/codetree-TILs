import java.util.*;
import java.io.*;

public class Main {
    // 상우하좌
    final static int[] DX = {-1, 0, 1, 0};
    final static int[] DY = {0, 1, 0, -1};

    static int l;       // 3 <= 체스판 크기 <= 40
    static int n;       // 1 <= 기사들의 수 <= 30
    static int q;       // 1<= 명령의 수 <= 100
    static int[][] arr;     // l * l 크기의 체스판(0: 빈칸, 1: 함정, 2: 벽)
    static Person[] personArr;        // 기사 배열
    static int[][] query;     // 명령 배열(0열: 기사 번호, 1열: 방향)
    static Stack<Integer> stack;        // 밀린 기사 번호를 저장하는 스택
    static boolean isMetWall;       // 미는 도중에 벽을 만나면
    static int[] damageArr;     // 기사별로 받은 데미지 양

    static class Person {
        int x;      // 좌측 상단 행
        int y;      // 좌측 상단 열
        int h;      // 높이
        int w;      // 가로
        int hp;     // 현재 체력

        Person(int x, int y, int h, int w, int hp) {
            this.x = x;
            this.y = y;
            this.h = h;
            this.w = w;
            this.hp = hp;
        }
    }

    public static boolean isValid(int x, int y) {
        return 1 <= x && x <= l && 1 <= y && y <= l;
    }

    /*
        이동한 칸에 있는 함정의 갯수만큼 데미지를 입는다
        num: 기사 번호
     */
    public static void damage(int num) {
        Person person = personArr[num];
        int startX = person.x;
        int startY = person.y;
        int endX = startX + person.h;
        int endY = startY + person.w;
        for(int x=startX; x<endX; x++) {        // 행
            for(int y=startY; y<endY; y++) {    // 열
                if(arr[x][y] == 1) {        // 함정이 있으면
                    person.hp--;        // 체력 감소
                    damageArr[num]++;       // 받은 데미지 증가
                }
            }
        }
    }

    /*
        위 또는 아래로 밀기
    */
    public static Stack<Integer> pushRow(int num, boolean[] isVisit, int start, int end, int row, int d) {
        Stack<Integer> resultStack = new Stack();
        for(int i=start; i<end; i++) {
            if(arr[row][i] == 2) {      // 벽이 있으면
                isMetWall = true;
                break;
            }
            // 다른 기사를 만나는지 탐색
            for(int k=1; k<=n; k++) {
                // 탐색할 기사가 자기 자신 또는 기사가 죽었거나 이미 만난 기사라면
                if(k == num || isDead(k) || isVisit[k]) {
                    continue;
                }
                if(isMetOther(k, row, i) && isAbleToPush(k, d)) {        // 다른 기사를 만났으면
                    isVisit[k] = true;
                    resultStack.push(k);        // 만난 기사를 스택에 추가
                }
            }
        }
        return resultStack;
    }

    /*
        왼쪽 또는 오른쪽으로 밀기
    */
    public static Stack<Integer> pushCol(int num, boolean[] isVisit, int start, int end, int col, int d) {
        Stack<Integer> resultStack = new Stack();

        for(int i=start; i<end; i++) {      // 행
            if(arr[i][col] == 2) {      // 벽이 있으면
                isMetWall = true;
                break;
            }
            // 다른 기사를 만나는지 탐색
            for(int k=1; k<=n; k++) {
                // 탐색할 기사가 자기 자신 또는 기사가 죽었거나 이미 만난 기사라면
                if(k == num || isDead(k) || isVisit[k]) {
                    continue;
                }
                if(isMetOther(k, i, col) && isAbleToPush(k, d)) {        // 다른 기사를 만났으면
                    isVisit[k] = true;
                    resultStack.push(k);        // 만난 기사를 스택에 추가
                }
            }
        }
        return resultStack;
    }

    /*
        격자 밖을 벗어나지 않고 밀 수 있다면, true 반환
        num: 기사 번호
        d: 방향
    */
    public static boolean isAbleToPush(int num, int d) {
        Person person = personArr[num];     // 기사 정보
        int startX = person.x + DX[d];
        int startY = person.y + DY[d];
        int endX = startX + person.h - 1;
        int endY = startY + person.w - 1;

        if(!isValid(startX, startY) || !isValid(endX, endY)) {      // 격자를 넘어가면
            return false;
        }
        return true;
    }

    /*
        다른 기사를 만났다면 true 반환
        num: 기사 번호
    */
    public static boolean isMetOther(int num, int x, int y) {
        Person other = personArr[num];
        int sx = other.x;
        int sy = other.y;
        int ex = other.x + other.h - 1;
        int ey = other.y + other.w - 1;

        if(sx <= x && x <= ex && sy <= y && y <= ey) {      // (x, y)가 다른 기사의 범위 내에 들어가면
            return true;
        }
        return false;
    }

    /*
        죽은 기사라면 true 반환
    */
    public static boolean isDead(int num) {
        if(personArr[num].hp <= 0) {
            return true;
        }
        return false;
    }

    /*
        연쇄적으로 밀릴 기사를 스택에 담기
    */
    public static void continuousMove(int num, int d) {
        stack = new Stack();
        Stack<Integer> tmpStack = new Stack();
        tmpStack.push(num);
        stack.push(num);        // 초기 기사를 스택에 추가
        boolean[] isVisit = new boolean[n + 1];     // 기사 방문 배열
        isVisit[num] = true;

        while(true) {
            if(d == 0 || d == 2) {        // 위/아래로 밀기
                int curNum = tmpStack.pop();
                Person person = personArr[curNum];     // 기사 정보
                int startX = person.x;
                int startY = person.y;
                int endY = startY + person.w;
                int row = startX - 1;       // 위로 밀때
                if(d == 2) {        // 아래로 밀때
                    row = startX + person.h;
                }
                Stack<Integer> resultStack = pushRow(num, isVisit, startY, endY, row, d);
                if(isMetWall) {     // 미는 도중에 벽을 만났으면
                    return;
                }
                if(tmpStack.isEmpty() && resultStack.isEmpty()) {     // 더이상 밀 기사가 없으면
                    return;
                }
                // 밀어야 할 기사를 스택에 추가
                tmpStack.addAll(resultStack);
                stack.addAll(resultStack);
            } else {        // 왼쪽/오른쪽으로 밀기
                int curNum = tmpStack.pop();
                Person person = personArr[curNum];     // 기사 정보
                int startX = person.x;
                int endX = startX + person.h;
                int startY = person.y;
                int col = startY - 1;       // 왼쪽으로 밀때
                if(d == 1) {        // 오른쪽으로 밀때
                    col = startY + person.w;
                }
                Stack<Integer> resultStack = pushCol(num, isVisit, startX, endX, col, d);
                if(isMetWall) {     // 미는 도중에 벽을 만났으면
                    return;
                }
                if(tmpStack.isEmpty() && resultStack.isEmpty()) {     // 더이상 밀 기사가 없으면
                    return;
                }
                // 밀어야 할 기사를 스택에 추가
                tmpStack.addAll(resultStack);
                stack.addAll(resultStack);
            }
        }
    }

    /*
        기사 이동
        num: 기사 번호
        d: 이동 방향
        이동하려는 곳에 다른 기사가 있으면 그 기사도 연쇄적으로 1칸씩 밀린다
        기사가 이동하는 방향 끝에 벽이 있으면 모든 기사는 이동 불가
        체스판에서 사라진 기사에게 명령 내리면 무시하기
    */
    public static void move(int num, int d) {
        if(isDead(num)) {      // 죽은 기사라면
            return;
        }
        isMetWall = false;
        if(!isAbleToPush(num, d)) {       // d방향으로 1칸 이동 시 격자를 벗어나면
            return;
        }
        continuousMove(num, d);       // 연쇄적으로 몇 개의 기사를 밀 것인지 탐색
        if(isMetWall) {     // 도중에 벽을 만났으면
            return;
        }
        while(!stack.isEmpty()) {
            Integer cur = stack.pop();      // 밀릴 기사 번호
            // 위치 갱신
            personArr[cur].x += DX[d];
            personArr[cur].y += DY[d];
            if(cur == num) {        // 명령을 받은 기사는 데미지를 입지 않는다
                break;
            }
            damage(cur);        // 받은 데미지 계산
        }
    }

    /*
        생존한 기사들이 받은 총 데미지의 합 출력
     */
    public static int printAnswer() {
        int answer = 0;
        for(int i=1; i<=n; i++) {       // 기사 번호
            if(personArr[i].hp > 0) {       // 생존한 기사라면
                answer += damageArr[i];
            }
        }
        return answer;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        l = Integer.parseInt(st.nextToken());
        n = Integer.parseInt(st.nextToken());
        q = Integer.parseInt(st.nextToken());

        // 체스판 정보 입력받기
        arr = new int[l + 1][l + 1];
        for(int x=1; x<=l; x++) {
            st = new StringTokenizer(br.readLine());
            for(int y=1; y<=l; y++) {
                arr[x][y] = Integer.parseInt(st.nextToken());
            }
        }

        // 초기 기사들의 정보 입력받기
        personArr = new Person[n + 1];
        damageArr = new int[n + 1];
        for(int i=1; i<=n; i++) {
            st = new StringTokenizer(br.readLine());
            int r = Integer.parseInt(st.nextToken());       // 초기 행 위치
            int c = Integer.parseInt(st.nextToken());       // 초기 열 위치
            int h = Integer.parseInt(st.nextToken());       // 높이
            int w = Integer.parseInt(st.nextToken());       // 가로 크기
            int k = Integer.parseInt(st.nextToken());       // 초기 체력
            personArr[i] = new Person(r, c, h, w, k);
        }

        // 명령 입력받기
        query = new int[q][2];
        for(int i=0; i<q; i++) {
            st = new StringTokenizer(br.readLine());
            query[i][0] = Integer.parseInt(st.nextToken());     // 기사 번호
            query[i][1] = Integer.parseInt(st.nextToken());     // 방향
        }

        for(int i=1; i<=q; i++) {
            move(query[i - 1][0], query[i - 1][1]);
        }

        // 생존한 기사들이 총 받은 데미지의 합 출력
        System.out.print(printAnswer());
    }
}