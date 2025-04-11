import java.io.*;
import java.util.*;

/*
 * 구현 완료 : 현재 2시간 8분
 * 1차 디버깅 : 13분
 */


class Main {
	public static void main(String args[]) throws Exception {
//		System.setIn(new FileInputStream("res/input.txt"));

		Scanner sc = new Scanner(System.in);
//		int T;
//		T = sc.nextInt();
//		for (int test_case = 1; test_case <= T; test_case++) {
			init(sc);
			
			simulate();
			
//			System.out.println(test_case+"번 답:");
			System.out.println(answerScore);
//		}
	}

	
	static int[] DR = {-1, 0, 1, 0};
	static int[] DC = {0, 1, 0, -1};
	
	static class Golrem{
		int[][] pos;
		int exitDir;
		
		Golrem(int cr, int cc, int exitDir){
			this.pos = new int[5][2];
			this.pos[4][0] = cr; 
			this.pos[4][1] = cc;
			this.exitDir=exitDir;

			for(int dir=0; dir<4; dir++) {
				pos[dir][0] = cr + DR[dir];
				pos[dir][1] = cc + DC[dir];
			}
		}
	}
	
	static Golrem[] golrems; // 1-based
	static int[][] grid; // 1-based
	static int K; // 정령 수
	static int R, C; // 행, 열
	static int MAXROW;
	static int CENTER=4;
	static int answerScore;
	
	static void init(Scanner sc) {
		R = sc.nextInt();
		C = sc.nextInt();
		K = sc.nextInt();
		
		MAXROW = R+3;
		golrems = new Golrem[K+1];
		grid = new int[MAXROW+1][C+1];
		answerScore = 0;
		
		for(int idx=1; idx<=K; idx++) {
			int c = sc.nextInt();
			int d = sc.nextInt();
			
			golrems[idx] = new Golrem(2, c, d);
		}
	}
	
	static boolean outOfBorder(int r, int c) {
		return r > MAXROW || c < 1 || c > C;
	}
	
	static void simulate() {
		for(int gidx=1; gidx <= K; gidx++) {
			if(!moveGolrem(gidx)) { // 숲 범위 나갔으면
				grid = new int[MAXROW+1][C+1];
				// System.out.println("숲범위 나감");
				continue;
			}
			// System.out.println("moveAngel 호출 전");
			// 숲 범위 안나갔으면
			moveAngel(gidx);
		}
	}
	
	private static void moveAngel(int gidx) {
		int sr = golrems[gidx].pos[CENTER][0];
		int sc = golrems[gidx].pos[CENTER][1];
		
		boolean[][] vis = new boolean[MAXROW+1][C+1];
		vis[sr][sc] = true;
		
		Deque<int[]> q = new ArrayDeque<>();
		q.offer(new int[] {sr, sc});
		
		int maxRow = 1;
		
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int nowr = now[0];
			int nowc = now[1];
			
			// 최대 행 처리
			if(maxRow < nowr) {
				maxRow = nowr;
			}
			
			for(int dir=0; dir<4; dir++) {
				int newr = nowr + DR[dir];
				int newc = nowc + DC[dir];
				
				if(outOfBorder(newr, newc)) continue;
				if(grid[newr][newc] < 1) continue; // 골렘 체크
				if(vis[newr][newc]) continue; // 방문 체크
				
				// 만약 현재 좌표가 출구라면 다른 골렘으로 이동 가능하다
				// 출구가 아니라면 자기 골렘 내에서만 이동 가능하다
				int golremIdx = grid[nowr][nowc];
				int exitDir = golrems[golremIdx].exitDir;
				int[] exitPos = golrems[golremIdx].pos[exitDir];

				// 다른 골렘으로 이동하는 거면
				if(grid[newr][newc] != golremIdx) {
					// 출구라면
					if(exitPos[0] == nowr && exitPos[1] == nowc) {
						vis[newr][newc] = true;
						q.offer(new int[] {newr, newc});
					}else { // 아니면 불가능
						continue;
					}
				}else {
					vis[newr][newc] = true;
					q.offer(new int[] {newr, newc});
				}
			}
		}
		
		if(maxRow - 3 < 4) {
			// System.out.println("정령 탐색 오류");
		}
		// System.out.println(maxRow);
		answerScore += (maxRow-3);
	}

	static boolean outOfForest(int gidx) {
		int CR = golrems[gidx].pos[CENTER][0];
		int CC = golrems[gidx].pos[CENTER][1];
		
		if(CR <= 4) return true;
		else return false;
	}
	
	static boolean moveGolrem(int gidx) {
//		int turncnt=0;
			while(true) {
	//			turncnt++;
				// System.out.println(turncnt+"번째 턴일때 ");
				// 아래 이동
				if(moveSouth(gidx)) {
					continue; // 계속 이동
				}else {
					// 반시계 회전
					if(rotateReTime(gidx)) {
						continue;
					}else {
						// 시계 회전
						if(rotateTime(gidx)) {
							continue;
						}else { 
							// 마지막 이동 실패
							// 숲 범위 나갔는지 확인 해야돼
							if(outOfForest(gidx)) { // 숲 범위 나감
								return false;
							}else {
								// 보드판 갱신
								renewBoard(gidx);
								return true;
							}	
						}
					}
				}
			}
	}

	private static void renewBoard(int gidx) {
		for(int bidx=0; bidx<5; bidx++) {
			int r = golrems[gidx].pos[bidx][0];
			int c = golrems[gidx].pos[bidx][1];
			
			if(grid[r][c] > 0) {
				System.out.println("보드판 갱신 실패");
				System.out.println("중복 좌표 갱신");
			}
			grid[r][c] = gidx;
		}
	}

	private static boolean moveSouth(int gidx) {
		Golrem golrem = golrems[gidx];
		
		// 밑 : 2
		boolean invalid = false;
		for(int blockidx=0; blockidx<5; blockidx++) {
			int r = golrem.pos[blockidx][0];
			int c = golrem.pos[blockidx][1];
			
			int nr = r + DR[2];
			int nc = c + DC[2];
			
			if(outOfBorder(nr, nc)) {
				invalid = true;
				break;
			}
			if(grid[nr][nc] > 0) {
				invalid = true;
				break;
			}
		}
		if(invalid) return false;
		
		// System.out.println("south 이동 가능");
		// 가능하면 이동
		for(int blockidx=0; blockidx<5; blockidx++) {
			int r = golrem.pos[blockidx][0];
			int c = golrem.pos[blockidx][1];
			
			int nr = r + DR[2];
			int nc = c + DC[2];
			
			golrem.pos[blockidx][0] = nr;
			golrem.pos[blockidx][1] = nc;
		}
		return true;
	}
	
	private static boolean rotateReTime(int gidx) {
		int[][] mustNeedPos = {
			{-1, -1}, {0, -2}, {1, -2}, {1, -1}, {2,-1}
		};
		
		int r = golrems[gidx].pos[CENTER][0];
		int c = golrems[gidx].pos[CENTER][1];
		
		// 이동 가능한지만 일단 확인
		for(int pidx=0; pidx<5; pidx++) {
			int nr = r + mustNeedPos[pidx][0];
			int nc = c + mustNeedPos[pidx][1];
			
			if(outOfBorder(nr,nc)) return false;
			if(grid[nr][nc] > 0) return false;
		}
		
		// System.out.println("반시계 회전 이동 가능");
		
		// 실제로 이동, 3(서)로 이동, 2(남) 이동
		for(int bidx=0; bidx<5; bidx++) {
			golrems[gidx].pos[bidx][0] += DR[3];
			golrems[gidx].pos[bidx][1] += DC[3];
			
			golrems[gidx].pos[bidx][0] += DR[2];
			golrems[gidx].pos[bidx][1] += DC[2];
		}
		
		// 출구 반시계 회전
		int exitDir = golrems[gidx].exitDir;
		exitDir = (exitDir - 1 + 1000 * 4) %4;
		golrems[gidx].exitDir = exitDir;
		
		return true;
	}
	
	private static boolean rotateTime(int gidx) {
		int[][] mustNeedPos = {
			{-1, 1}, {0, 2}, {1, 2}, {1, 1}, {2, 1}
		};
		
		int r = golrems[gidx].pos[CENTER][0];
		int c = golrems[gidx].pos[CENTER][1];
		
		// 이동 가능한지만 일단 확인
		for(int pidx=0; pidx<5; pidx++) {
			int nr = r + mustNeedPos[pidx][0];
			int nc = c + mustNeedPos[pidx][1];
			
			if(outOfBorder(nr,nc)) return false;
			if(grid[nr][nc] > 0) return false;
		}
		
//		System.out.println("회전 이동 가능");
		
		// 실제로 이동, 1(동)로 이동, 2(남) 이동
		for(int bidx=0; bidx<5; bidx++) {
			golrems[gidx].pos[bidx][0] += DR[1];
			golrems[gidx].pos[bidx][1] += DC[1];
			
			golrems[gidx].pos[bidx][0] += DR[2];
			golrems[gidx].pos[bidx][1] += DC[2];
		}
		
		// 출구 반시계 회전
		int exitDir = golrems[gidx].exitDir;
		exitDir = (exitDir + 1 + 1000 * 4) %4;
		golrems[gidx].exitDir = exitDir;
		
		return true;
	}
}