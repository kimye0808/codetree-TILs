import java.io.*;
import java.util.*;

/*
 * 설계 : 1시간 18분
 * 구현 끝 : 2시간 30분
 * 1차 디버깅 완 : 3시간 20분
 * 어렵다
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
