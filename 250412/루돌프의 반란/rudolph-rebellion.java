import java.io.*;
import java.util.*;

/*
 * 설계 : 1시간 18분
 * 구현 끝 : 2시간 30분
 * 1차 디버깅 완 : 3시간 20분
 * 어렵다
 설계 미스가 많았음 특히 충돌 + 연쇄 부분에서 객체 상태와 보드판 갱신에서
 */
/*
 * 산타는 1번 부터 P번 P명
 * 게임판 : N*N, 1-based
 * M개의 턴, 매 턴마다 루돌프와 산타 한 번씩 이동
 * 루돌프 한 번 이동 -> 1~P번 산타 순서대로 이동(기절or격자 밖 산타 이동X)
 * 
 * 거리 = Math.pow(r1-r2, 2) + Math.pow(c1-c2, 2)
 * 
 * 2. 루돌프의 이동
 * - 가장 가까운 산타 (탈락하지 않은 산타 중), 2명 이상이면 r 좌표가 큰, r이 동일하면 c좌표 큰
 * - 향해 1칸 이동
 * - 상하좌우, 대각선 총 인접 8방향 이동 가능
 * 2-1. 가장 가까운 산타 선택
 * 2-2. 해당 산타를 향해 8방향 중 가장 가까워지는 방향으로 한 칸
 * 
 * 3. 산타의 이동 : 상우하좌
 * - 산타는 1~P 순서대로 
 * - 기절 or 탈락 산타는 이동불가
 * - 루돌프로 가장 가까워지는 방향
 * - 으로 한 칸 이동
 * - 다른 산타나 게임판 밖으로는 이동 불가
 * - 움직일 수 없다면 이동X
 * - 움직일 수 있더라도 루돌프에 가까워지는 방향 없다면 이동 X
 * - 상하좌우 인접 4방향 중 한 곳, 우선순위 : 상우하좌
 * 
 * 4. 충돌
 * - 산타와 루돌프가 같은 칸
 * 4-1. 루돌프 -> 산타 충돌: 산타 +C점수, 
 * 4-1-1. 산타는 루돌프 이동 방향으로 C 만큼 이동
 * 4-2. 산타 -> 루돌프: 산타 +D 점수, 
 * 4-2-1. 산타의 이동해온 반대 방향으로 D 만큼 이동
 * 4-3. 밀려나는 중에는 충돌X 해당 좌표로 이동
 * 4-4. 산타가 밀려난 위치가 게임판 밖이면 산타 탈락
 * 4-5. 산타가 밀려난 칸에 다른 산타 -> 상호작용
 * 
 * 5. 상호작용
 * - 루돌프와 산타 충돌해서 밀려난 산타가 착지한 칸에 산타 
 * 5-1. 착지하는 산타 말고 해당 위치 산타가 1칸 해당 방향으로 이동
 * 5-2. 이동한 곳에 다른 산타 있으면 거기 있던 산타가 1칸 또 이동
 * 5-3. 게임판 밖까지 밀려온 산타는 탈락
 * 
 * 6. 기절
 * 6-1. 산타는 루돌프와 충돌 후 기절
 * 6-2. k번째 턴에 충돌하면 k+2에서야 이동 가능
 * 6-3. 기절한 도중 충돌이나 상호작용 가능
 * 6-4. 루돌프가 기절한 산타를 돌진 대상으로도 가능
 * 
 * 7. 종료
 * 7-1. M턴 또는 P명의 산타 모두 탈락,
 * 7-2. 매 턴 이후 아직 탈락하지 않은 산타들은 1점씩 부여
 * 7-3. 각 산타가 얻은 최종 점수
 */
 /*
 * SIZE = 게임판 크기
 * MAXTURN = 게임 턴 수
 * SANCNT = 산타 수
 * RUPOWER = 루돌프 힘
 * SANPOWER = 산타힘
 * 각 산타가 얻은 최종 점수 1~P 번 산타 순서대로 출력
 * 거리 = Math.pow(r1-r2, 2) + Math.pow(c1-c2, 2)
 * 
 * 객체 + 보드판 둘 다 처리해줘야 함 <- 항상 고려해줘야 한다 주의
 * 
 * 루돌프 객체: r, c
 * 산타 객체: r, c, sturned:int, isDead
 * board[][] : 1-based
 * List<San>
 * List<Ru>
 * 
 * 루돌프 방향과 산타 방향은 달라도 편하다, 어차피 둘 중 하나 방향 적용하는 거기 때문
 * 
 * RDR, RDC: 8방향
 * 
 * 산타 PRIORITY 방향 우선순위 : TOP, RIGHT, BOTTOM, LEFT
 * TOP = 0, RIGHT = 1, LEFT = 2 BOTTOM = 3
 * 산타 방향 = 상, 우, 좌, 하
 * 반대 이동 편하게: 3-dir하면 됨 SDR[3-PRIOR(3)] = RIGHT 
 * SDR[PRIOR[0]] = 상
 * 
 * 루돌프 이동 -> 산타 이동(충돌, 상호작용...)
 * RU = P+1
 * 
 * ETC. findCloseSan
 * 1. for r 내림차순
 * 2. for c 내림차순
 * 3. board가 1~P면 산타, 거리 최소 찾기
 * 4. 해당 산타 idx 리턴
 * 
 * ETC. getDirToSan: sanIdx
 * 1. 8방향 dir에서 가장 가까운 찾기
 * 2. 리턴
 * 
 * ETC. getDirToRu:nowr, nowc
 * 0. nowr, nowc와 ru 거리 계산
 * 1. 4방향 산타방향, newr, newc에서 거리 계산, 가장 가까운 방향 찾기
 * 2. 0에서 구한 ru 거리 보다 작으면 방향 리턴
 * 3. 아니면 -1리턴
 * 
 * 1. moveRu - 8 방향
 * 1-1. 탈락하지 않은 산타 중, 가장 가까운 산타 찾기, 거리 계산 이용
 * 1-1-1. 우선순위 : r 좌표가 큰, r이 동일하면 c 좌표가 큰
 * 1-2. 선택한 산타로 가는 8방향으로 1칸 이동한 nr, nc에서 거리 계산
 * 1-2-1. 가장 가까워지는 방향 구하기
 * 1-3. 해당 방향으로 이동할떄
 * 1-3-1. 객체 상태 갱신, 이동 전 좌표 board 는 0 
 * 1-4. 해당 board가 1~P면 ruHitSan
 * 
 * 2. moveSanta
 * 2-1. 산타 리스트 순회
 * 2-2. (sturned가 0이 아니고 sturned+2 > turn이면) 또는 dead 는 이동X
 * 2-3. nr, nc해서 거리 계산, 루돌프로 가장 가까워지는 방향
 * 2-4. 한 칸 이동
 * 2-5. 다른 산타 or 격자 밖 이동 불가
 * 2-6. 못움직이면 이동X
 * 2-7. 이동가능 하더라도 루돌프 가까워지는 방향 없으면 X
 * 2-8. 방향 우선순위 : 상우하좌
 * 
 * 3. ruHitSan
 * 3-0. 루돌프가 이동할 곳에 newRuR, newRuC 에 board[][]가 1이상 P이하인 경우
 * 3-2. 해당 산타의 점수 +RUPOWER, 산타 루돌프 방향으로 C만큼 nSanR, nSanC
 * 3-3. 만약 nSanR, nSanC가 outOfBorder-> 산타 탈락
 * 3-4. 만약 nSanR, nSanC에 산타 존재, 
 * 3-4-0. 객체 상태 갱신, 좌표 + 스턴, 이동 전 좌표 board 0
 * 3-4-1. interact(sanIdx, nSanR, nSanC, RuDir)
 * 3-4-2. newRuR, newRuC board는 RU
 * 
 * 4. sanHitRu
 * 4-0. 산타가 이동할 곳에 newSanR, newSanC 에 board[][]가 RU 인 경우
 * 4-1. 산타 점수 +SANPOWER, 산타 이동 반대 SDR[3-PRIOR(dir)] 로 이동
 * 4-2. 만약 격자 밖 -> dead, 
 * 4-3. 만약 산타 존재,
 * 4-4. 미는 산타 상태 갱신, 좌표 + 스턴, 이동 전 좌표 board 0
 * 4-5. interact(sanIdx, 동일)
 * 
 * 5. interact : sanIdx(미는 산타idx), nowR, nowC 
 * 5-1-1. nowR nowC가 격자 밖이면 dead 처리 <- 기저
 * 5-1-2. 또는 board[][] 가 0이면,
 * 5-1-3. board 갱신해주고 리턴
 * 5-2. targetR, targetC에 산타 있으면 밀린 산타는
 * 5-2-1. 1칸 이동한 nSanR, nSanC로 객체 상태 갱신 
 * 5-2-2. 다시 interact 반복
 * 5-2-3. nowR, nowC board[][]에 sanIdx로 갱신
 */
public class Main {
	public static void main(String[] args) throws Exception {
//		System.setIn(new FileInputStream("res/input.txt"));
		Scanner sc = new Scanner(System.in);
//		int T;
//		T = sc.nextInt();
//		for (int test_case = 1; test_case <= T; test_case++) {
//			StringBuilder sb = new StringBuilder();
//			sb.append("#").append(test_case).append(" ");
			init(sc);
			
			simulate();
			
//			for(int sidx=1; sidx<=SANCNT; sidx++) {
//				sb.append(scores[sidx]).append(" ");
//			}
			for(int sidx=1; sidx<=SANCNT; sidx++) {
				System.out.print(scores[sidx]+" ");
			}
//		}
	}
	
	// 루돌프 방향
	static int[] RDR = {-1, -1, 0, 1, 1, 1, 0, -1};
	static int[] RDC = {0, 1, 1, 1, 0, -1, -1, -1};
	// 산타 방향
	static int TOP = 0, RIGHT = 1, LEFT = 2, BOTTOM = 3;
	static int[] PRIOR = {TOP, RIGHT, BOTTOM, LEFT}; // 우선순위는 상우하좌
	static int[] SDR = {-1, 0, 0, 1};
	static int[] SDC = {0, 1, -1, 0};
	
	static int SIZE;
	static int MAXTURN;
	static int SANCNT;
	static int RUPOWER;
	static int SANPOWER;
	
	static class Ru{
		int r, c;
		Ru(int r, int c){
			this.r=r;this.c=c;
		}
	}
	static class San{
		int r, c, sturned;
		boolean isDead;
		San(int r, int c){
			this.r=r;this.c=c;
			this.sturned=0;
			this.isDead=false;
		}
	}
	static int[] scores;
	static int[][] board;
	static San[] sans;
	static Ru ru;
	static int RU;
	static int turn;
	
	static void init(Scanner sc) {
		SIZE = sc.nextInt();
		MAXTURN = sc.nextInt();
		SANCNT = sc.nextInt();
		RUPOWER = sc.nextInt();
		SANPOWER = sc.nextInt();
		
		RU = SANCNT+1;
		board = new int[SIZE+1][SIZE+1];
		sans = new San[SANCNT+1];
		scores = new int[SANCNT+1];
		
		int rr = sc.nextInt();
		int rc = sc.nextInt();
		ru = new Ru(rr,rc);
		board[rr][rc]=RU;
		
		for(int idx=1; idx<=SANCNT; idx++) {
			int sanIdx = sc.nextInt();
			int sr = sc.nextInt();
			int sy = sc.nextInt();
			
			sans[sanIdx] = new San(sr, sy);
			board[sr][sy] = sanIdx;
		}
	}
	
	static int calcDist(int fr, int fc, int tr, int tc) {
		return (int) (Math.pow(fr-tr, 2) + Math.pow(fc-tc, 2));
	}
	
	static boolean outOfBorder(int r, int c) {
		return r < 1 || c < 1 || r > SIZE || c > SIZE;
	}
	
	static void printBoard(String msg) {
		System.out.println(msg);
		for(int r=1; r<=SIZE; r++) {
			for(int c=1; c<=SIZE; c++) {
				System.out.print(board[r][c]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	static void printSans() {
		for(int sidx=1; sidx<=SANCNT; sidx++) {
			if(sans[sidx].isDead) continue;
			System.out.println(sidx+"번 산타 :"+sans[sidx].r +" , "+ sans[sidx].c);
		}
		System.out.println();
	}
	
	static void simulate() {
		for(turn=1; turn<=MAXTURN; turn++) {
			
			moveRu();
			
			for(int sidx=1; sidx<=SANCNT; sidx++) {
				if(sans[sidx].sturned != 0 && (sans[sidx].sturned + 2 > turn)) continue;
				if(sans[sidx].isDead) continue;
				
				moveSan(sidx);	
			}
			
			int deadCnt=0;
			for(int sidx=1; sidx<=SANCNT; sidx++) {
				if(sans[sidx].isDead) {
					deadCnt++;
					continue;
				}
				scores[sidx]++;
			}
			if(deadCnt == SANCNT) break;
		}
	}
	
	private static void moveSan(int sidx) {
		int r = sans[sidx].r;
		int c = sans[sidx].c;
		
		int aptDir = getDirToRu(sidx);
		if(aptDir == -1) return; // 갈 곳 없으면 이동X
		
		int nr = r + SDR[PRIOR[aptDir]];
		int nc = c + SDC[PRIOR[aptDir]];
		
		board[r][c] = 0;
		sans[sidx].r = nr;
		sans[sidx].c = nc;
		
		if(board[nr][nc] == RU) {
			aptDir = 3 - PRIOR[aptDir];
			sanHitRu(sidx, nr, nc, aptDir);
		}else {
			// ru가 아닐 경우에도 보드판 갱신
			board[nr][nc] = sidx;
		}

	}

	private static void sanHitRu(int sanIdx, int nr, int nc, int sanDir) {
		scores[sanIdx] += SANPOWER;
		
		int nowSanR = sans[sanIdx].r;
		int nowSanC = sans[sanIdx].c;

		int newSanR = nowSanR + SANPOWER * SDR[sanDir];
		int newSanC = nowSanC + SANPOWER * SDC[sanDir];
		
		if(outOfBorder(newSanR, newSanC)) {
			sans[sanIdx].isDead = true;
			return;
		}
		
		sans[sanIdx].r = newSanR;
		sans[sanIdx].c = newSanC;
		sans[sanIdx].sturned = turn;
		if(board[newSanR][newSanC] >= 1 && board[newSanR][newSanC] <= SANCNT) {
			interactSanToRu(sanIdx, newSanR, newSanC, sanDir);
		}
		board[newSanR][newSanC] = sanIdx;
	}

	private static void interactSanToRu(int sanIdx, int nowR, int nowC, int targetDir) {
		if(outOfBorder(nowR, nowC)) {
			sans[sanIdx].isDead = true;
			return;
		}
		if(board[nowR][nowC] == 0) {
			board[nowR][nowC] = sanIdx;
			return;
		}
		
		if(board[nowR][nowC] >= 1 && board[nowR][nowC] <= SANCNT) {
			int nextSanIdx = board[nowR][nowC]; // 밀리는 산타
			
			int nextR = nowR + SDR[targetDir];
			int nextC = nowC + SDC[targetDir];
			
			sans[nextSanIdx].r = nextR;
			sans[nextSanIdx].c = nextC;
			
			interactSanToRu(nextSanIdx, nextR, nextC, targetDir);
		}
		
		board[nowR][nowC] = sanIdx; // 미는 산타
	}
	

	static void moveRu() {
		int targetIdx = findCloseSan();
		
		int aptDir = getDirToSan(targetIdx);
		
		int nr = ru.r + RDR[aptDir];
		int nc = ru.c + RDC[aptDir];

		// 이전 좌표 보드판 0
		board[ru.r][ru.c] = 0;
		// 루돌프 상태 갱신
		ru.r = nr;
		ru.c = nc;
		// 이동 후의 보드판 체크
		if(board[nr][nc] >= 1 && board[nr][nc] <= SANCNT) {
			ruHitSan(ru.r, ru.c, aptDir);
		}
			board[nr][nc] = RU;
	}

	private static void ruHitSan(int r, int c, int ruDir) {
		int sanIdx = board[r][c];
		scores[sanIdx] += RUPOWER;
		
		int nowSanR = sans[sanIdx].r;
		int nowSanC = sans[sanIdx].c;
		int newSanR = nowSanR + RUPOWER * RDR[ruDir];
		int newSanC = nowSanC + RUPOWER * RDC[ruDir];
		
		if(outOfBorder(newSanR, newSanC)) {
			sans[sanIdx].isDead = true;
			return;
		}
		
		sans[sanIdx].r = newSanR;
		sans[sanIdx].c = newSanC;
		sans[sanIdx].sturned = turn;
		if(board[newSanR][newSanC] >= 1 && board[newSanR][newSanC] <= SANCNT) {
			interactRuToSan(sanIdx, newSanR, newSanC, ruDir);
		}
		board[newSanR][newSanC] = sanIdx;
		board[nowSanR][nowSanC] = RU;
	}

	private static void interactRuToSan(int sanIdx, int nowR, int nowC, int targetDir) {
		if(outOfBorder(nowR, nowC)) {
			sans[sanIdx].isDead = true;
			return;
		}
		if(board[nowR][nowC] == 0) {
			sans[sanIdx].r = nowR;
			sans[sanIdx].c = nowC;
			board[nowR][nowC] = sanIdx;
			return;
		}
		
		if(board[nowR][nowC] >= 1 && board[nowR][nowC] <= SANCNT) {
			int nextSanIdx = board[nowR][nowC]; // 밀리는 산타
			
			int nextR = nowR + RDR[targetDir];
			int nextC = nowC + RDC[targetDir];
			
			sans[nextSanIdx].r = nextR;
			sans[nextSanIdx].c = nextC;
			

			interactRuToSan(nextSanIdx, nextR, nextC, targetDir);
		}
		
		board[nowR][nowC] = sanIdx; // 미는 산타
	}

	private static int getDirToSan(int sanIdx) {
		int nowr= ru.r;
		int nowc= ru.c;
		int minDist = SIZE * SIZE;
		int aptDir = -1;
		
		for(int dir=0; dir<8; dir++) {
			int newr = nowr + RDR[dir];
			int newc = nowc + RDC[dir];
			
			if(outOfBorder(newr, newc)) continue;
			
			int localDist = calcDist(newr, newc, sans[sanIdx].r, sans[sanIdx].c);
			if(minDist > localDist) {
				minDist = localDist;
				aptDir = dir;
			}
		}
		
		if(aptDir == -1) {
			System.out.println("ru가 target santa로 방향 못찾음 에러");
		}
		return aptDir;
	}
	
	private static int getDirToRu(int sanIdx) {
		int nowr= sans[sanIdx].r;
		int nowc= sans[sanIdx].c;
		int minDist = calcDist(ru.r, ru.c, sans[sanIdx].r, sans[sanIdx].c);
		int aptDir = -1;
		
		for(int dir=0; dir<4; dir++) {
			int newr = nowr + SDR[PRIOR[dir]];
			int newc = nowc + SDC[PRIOR[dir]];
			
			if(outOfBorder(newr, newc)) continue;
			if(board[newr][newc] >= 1 && board[newr][newc] <= SANCNT) continue;
			
			int localDist = calcDist(newr, newc, ru.r, ru.c);
			if(minDist > localDist) {
				minDist = localDist;
				aptDir = dir;
			}
		}
		
		return aptDir;
	}

	private static int findCloseSan() {
		int minDist = SIZE*SIZE;
		int targetIdx = 0;
		for(int r=SIZE; r>=1; r--) {
			for(int c=SIZE; c>=1; c--) {
				if(board[r][c] >= 1 && board[r][c] <= SANCNT) {
					int sanIdx = board[r][c];
					if(sans[sanIdx].isDead) continue; // 죽으면 패스
					
					int localDist = calcDist(ru.r, ru.c, r, c);
					if(minDist > localDist) {
						minDist = localDist;
						targetIdx = board[r][c];
					}
				}
			}
		}
		if(targetIdx == 0) {
			System.out.println("루돌프가 산타 못찾음 에러");
		}
		return targetIdx;
	}
}
