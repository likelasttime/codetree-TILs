import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static final int MAX_M = 100000;
    public static final int MAX_N = 100000;

    public static Scanner sc;

    public static int n, m, q;      // 벨트의 갯수, 물건의 갯수, 명령의 수

    // id에 해당하는 상자의 nxt 값과 prv 값을 관리
    // 0이면 없다.
    public static int[] prv = new int[MAX_M + 1];
    public static int[] nxt = new int[MAX_M + 1];

    // 각 벨트별로 head, tail id, 그리고 총 선물 수를 관리
    public static int[] head = new int[MAX_N];
    public static int[] tail = new int[MAX_N];
    public static int[] numGift = new int[MAX_N];

    /*
        공장 설립
    */
    public static void buildFactory() {
        // 공장 정보를 입력받기
        n = sc.nextInt();   // 벨트의 갯수  
        m = sc.nextInt();   // 물건의 갯수

        // 벨트의 정보를 만들기
        ArrayList<Integer>[] boxes = new ArrayList[n];
        for(int i=0; i<n; i++) {
            boxes[i] = new ArrayList<>();
        }

        for(int id=1; id<=m; id++) {   // 선물의 번호 
            int bNum = sc.nextInt();    // 벨트 번호
            bNum--;

            boxes[bNum].add(id);    // bNum번 벨트에 id번 선물 추가
        }

        // 초기 벨트의 head, tail, nxt, prv 값을 설정
        for(int i=0; i<n; i++) {        // 벨트 번호
            // 비어있는 벨트라면 패스
            if(boxes[i].size() == 0) {
                continue;
            }

            // head, tail을 설정
            head[i] = boxes[i].get(0);  // i번째 벨트에서 첫 번째 선물
            tail[i] = boxes[i].get(boxes[i].size() - 1);    // i번째 벨트에서 마지막 선물

            // 벨트 내 선물 총 수를 관리
            numGift[i] = boxes[i].size();

            // nxt, prv를 설정
            for(int j=0; j<boxes[i].size()-1; j++) {
                nxt[boxes[i].get(j)] = boxes[i].get(j + 1);     // 오른쪽 선물 저장
                prv[boxes[i].get(j + 1)] = boxes[i].get(j);     // 왼쪽 선물 저장
            }
        }
    }

    /*
        물건을 전부 옮긴다.
    */
    public static void move() {
        int mSrc = sc.nextInt();    
        int mDst = sc.nextInt();
        mSrc--; mDst--;

        // mSrc에 물건이 없다면, 그대로 mDst내 물건 수가 답이 된다.
        if(numGift[mSrc] == 0) {
            System.out.println(numGift[mDst]);      // mDst 벨트에 있는 물건 수 출력
            return;
        }

        // mDst에 물건이 없다면 그대로 옮겨주기
        if(numGift[mDst] == 0) {
            head[mDst] = head[mSrc]; // mDst 벨트의 맨 앞 선물 번호 = mSrc 벨트의 맨 앞 선물 번호
            tail[mDst] = tail[mSrc]; // mDst 벨트의 맨 뒤 선물 번호 = mSrc 벨트의 맨 뒤 선물 번호
        } else {    // mDst에 물건이 있다면
            int origHead = head[mDst];  // mDst 벨트의 맨 앞 선물 번호
            head[mDst] = head[mSrc];    // mDst의 head를 교체
            // mSrc의 tail과 기존 mDst의 head를 연결
            int srcTail = tail[mSrc];   
            nxt[srcTail] = origHead;
            prv[origHead] = srcTail;
        }

        // head, tail을 비워주기
        head[mSrc] = tail[mSrc] = 0;

        // 선물 상자 수를 갱신
        numGift[mDst] += numGift[mSrc];
        numGift[mSrc] = 0;  // mSrc 벨트에 있는 선물 상자 수를 초기화

        System.out.println(numGift[mDst]);  
    }

    /*
        해당 벨트의 head를 제거
    */
    public static int removeHead(int bNum) {
        // 불가능하면 패스
        if(numGift[bNum] == 0) {
            return 0;
        }

        // 노드가 1개라면 head, tail을 전부 삭제 후 반환
        if(numGift[bNum] == 1) {
            int id = head[bNum];    // bNum 벨트의 맨 앞 선물 번호
            head[bNum] = tail[bNum] = 0;    // bNum 벨트에 있는 선물을 없애기
            numGift[bNum] = 0;  // bNum 벨트에 있는 선물 수 초기화
            return id;
        }

        // head를 바꿔주기
        int hid = head[bNum];   // bNum 벨트의 맨 앞에 있는 선물 번호
        int nextHead = nxt[hid];    // hid 선물의 왼쪽에 있는 선물 번호
        nxt[hid] = prv[nextHead] = 0;   // hid의 오른쪽, nextHead의 왼쪽을 초기화
        numGift[bNum]--;    // bNum번 선물의 수를 1 감소
        head[bNum] = nextHead;  // bNum 벨트의 맨 앞 선물을 nextHead로 변경

        return hid;
    }

    /*
        해당 벨트에 head를 추가
    */
    public static void pushHead(int bNum, int hid) {
        // 불가능한 경우는 진행하지 않는다.
        if(hid == 0) {
            return;
        }

        // 비어있었다면, head, tail이 동시에 추가됨
        if(numGift[bNum] == 0) {
            head[bNum] = tail[bNum] = hid;
            numGift[bNum] = 1;
        } else {    // 그렇지 않다면 head만 교체
            int origHead = head[bNum];  // bNum 벨트에서 첫 번째 선물 번호
            nxt[hid] = origHead;    // hid번 선물의 오른쪽 = origHead 선물
            prv[origHead] = hid;    // origHead번 선물의 왼쪽 = hid 선물
            head[bNum] = hid;   // bNum 벨트에 있는 첫 번째 선물 = hid 선물
            numGift[bNum]++;    // bNum 벨트에 있는 선물 수 증가
        }
    }

    /* 
        앞 물건을 교체
    */
    public static void change() {
        int mSrc = sc.nextInt();
        int mDst = sc.nextInt();
        mSrc--; mDst--;

        int srcHead = removeHead(mSrc);     // mSrc 벨트에서 맨 앞 선물 번호
        int dstHead = removeHead(mDst);     // mDst 벨트에서 맨 앞 선물 번호
        pushHead(mDst, srcHead);    // mDst 벨트의 앞에 srcHead 선물을 추가
        pushHead(mSrc, dstHead);    // mSrc 벨트의 앞에 dstHead 선물을 추가

        System.out.println(numGift[mDst]);
    }

    /*
        물건을 나눠 옮겨주기
    */
    public static void divide() {
        int mSrc = sc.nextInt();
        int mDst = sc.nextInt();
        mSrc--; mDst--;

        // 순서대로 src에서 박스들을 빼주기
        int cnt = numGift[mSrc];        // mSrc 벨트에 있는 선물의 수
        ArrayList<Integer> boxIds = new ArrayList<>();
        for(int i=0; i<cnt/2; i++) {
            boxIds.add(removeHead(mSrc));   // mSrc 벨트에서 맨 앞 선물을 꺼내서 리스트에 저장
        }

        // 거꾸로 뒤집어서 하나씩 dst에 박스들을 넣어주기
        for(int i=boxIds.size()-1; i>=0; i--) {
            pushHead(mDst, boxIds.get(i));  // mDst 벨트에 선물 추가
        }

        System.out.println(numGift[mDst]);
    }

    /*
        선물 점수를 얻는다.
    */
    public static void giftScore() {
        int pNum = sc.nextInt();

        int a = prv[pNum] != 0 ? prv[pNum] : -1;    // pNum 선물의 왼쪽 선물 번호
        int b = nxt[pNum] != 0 ? nxt[pNum] : -1;    // pNum 선물의 오른쪽 선물 번호

        System.out.println(a + 2 * b);
    }

    /*
        벨트 정보를 얻기
    */
    public static void beltScore() {
        int bNum = sc.nextInt();
        bNum--;

        int a = head[bNum] != 0 ? head[bNum] : -1;  // bNum번 벨트에서 맨앞 선물 번호
        int b = tail[bNum] != 0 ? tail[bNum] : -1;  // bNum번 벨트에서 맨뒤 선물 번호
        int c = numGift[bNum];  // bNum번 벨트에 있는 총 선물 수

        System.out.println(a + 2 * b + 3 * c);
    }

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        q = sc.nextInt();   // 명령 수
        while(q-- > 0) {
            int qType = sc.nextInt();       // 명령 종류
            if(qType == 100) {  // 공장 설립
                buildFactory();
            } else if(qType == 200) {   // 물건 모두 옮기기
                move();
            } else if(qType == 300) {   // 앞 물건만 교체
                change();
            } else if(qType == 400) {   // 물건 나누기
                divide();
            } else if(qType == 500) {   // 선물 정보 얻기
                giftScore();
            } else {    // 벨트 정보 얻기
                beltScore();
            }
        }
    }

}