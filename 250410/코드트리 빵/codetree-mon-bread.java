import java.io.*;
import java.util.*;

/*
 * 설계: 36분
 * 구현 + 1차 디버깅 완 : 1시간 58분
 */
/*
격자판 board[][] - n*n - one-based
n = SIZE
m = MANCNT
사람 - Man : r, c, time, done
Man[] mans : 사람 배열

int[][] stores : m * 2 사람 별 편의점 좌표
int[][] camps : m * 2 사람 별 베이스캠프 좌표
boolean[][] cannotgo

def findClosePathStore:mIdx
1. mIdx에 해당하는 편의점 좌표 가져오기
2. 이제는 mIdx 사람의 좌표에서 시작
3. cannotgo 체크된 곳은 가지 못한다
4. 최소 거리로 이동 도착까지
5. 다음 좌표 리턴

def moveMans
1. mans 순회
1-1. man의 done이라면 이동하지 않는다
2. findclosepathstore 호출해서 다음 좌표 얻어오기
3. 좌표 갱신
4. 편의점이면 done 갱신
5. cannotgo 갱신

def findCloseCamp:mIdx,
1. mIdx에 해당하는 편의점 좌표 가져오기
2. 해당 좌표로부터 bfs, cannotgo 인데는 가지 못한다
3. board에서 1인 곳(베이스캠프)을 찾는다 
4. 이때 가장 가까운 캠프 여러개를 찾더라도 해당 캠프랑 거리가 같으면 계속 찾아야 한다
5. 같으면 행, 열까지 비교 한다
6. 해당 좌표를 리턴한다

def gotoCamp: 
1. mans 순회, 해당 시간 대의 사람이 있는지 확인
2. 해당 사람은 findcloseCamp를 호출, 캠프 좌표로 갱신한다
3. cannotgo 갱신
 */
class Main
{
	public static void main(String args[]) throws Exception
	{
		// System.setIn(new FileInputStream("res/input.txt"));

		Scanner sc = new Scanner(System.in);
		// int T;
		// T=sc.nextInt();
		
		// for(int test_case = 1; test_case <= T; test_case++)
		// {
		// 	StringBuilder sb = new StringBuilder();
		// 	sb.append("#").append(test_case).append(" ");
			init(sc);
			
			int answer = doSimulate();
			System.out.println(answer);
			// sb.append(answer);
			// System.out.println(sb.toString());
		// }
	}
	
	
	/*
	 격자판 board[][] - n*n - one-based
	n = SIZE
	m = MANCNT
	사람 - Man : r, c, time, done
	Man[] mans : 사람 배열
	
	int[][] stores : m * 2 사람 별 편의점 좌표
	int[][] camps : m * 2 사람 별 베이스캠프 좌표
	boolean[][] cannotgo
	 */
	static class Man{
		int r, c, time;
		boolean done;
		boolean onField; // 격자 내부에 있냐
		Man(int time){
			this.time=time;
			this.done=false;
			this.onField=false;
		}
		Man(int r, int c, int time){
			this.r=r;this.c=c;this.time=time;
			this.done=false;
			this.onField=false;
		}
	}
	
	static int[] DR = {-1, 0, 0, 1};
	static int[] DC = {0, -1, 1, 0};
	
	static int N; // 격자 크기 n
	static int M; // 사람의 수
	
	static int[][] board;
	static Man[] mans;
	static int[][] stores;
	static int[][] camps;
	static boolean[][] cannotgo;
	 
	
	static void init(Scanner sc) {
		N = sc.nextInt();
		M = sc.nextInt();
		
		board = new int[N+1][N+1]; // 1-based
		mans = new Man[M+1]; // 1-based
		for(int midx=1; midx<=M; midx++) {
			mans[midx] = new Man(midx);
		}
		stores = new int[M+1][2];
		cannotgo = new boolean[N+1][N+1];
		
		int campCnt=0;
		for(int row=1; row<=N; row++) {
			for(int col=1; col<=N; col++) {
				board[row][col] = sc.nextInt();
				if(board[row][col] == 1) campCnt++;
			}
		}
		// 캠프 초기화
		camps = new int[campCnt+1][2];
		
		// 편의점 입력
		for(int idx=1; idx<=M; idx++) {
			stores[idx][0] = sc.nextInt();
			stores[idx][1] = sc.nextInt();
		}
	}
	
	static boolean outOfBorder(int r, int c) {
		return r < 1 || c < 1 || r > N || c > N;
	}
	
	static void printManPos(String name) {
		System.out.println(name);
		for(int midx=1; midx<=M; midx++) {
			System.out.println(midx+"번 사람 : ");
			System.out.println(mans[midx].r+" , "+mans[midx].c);
		}
	}
	
	static int doSimulate() {
		int answer=0;
		int time = 1;
		while(true) {
			// 1, 2
			moveMans();
			
			// printManPos(time+"시간");
			// 3.
			gotoCamp(time);
			
			// check done
			if(checkDone()) {
				answer = time;
				break;
			}
			time++;
		}
		return answer;
	}
	
	private static boolean checkDone() {
		for(int midx=1; midx<=M; midx++) {
			if(!mans[midx].done) {
				return false;
			}
		}
		return true;
		
	}

	/*
	def findClosePathStore:mIdx
	1. mIdx에 해당하는 편의점 좌표 가져오기
	2. 이제는 mIdx 사람의 좌표에서 시작
	3. cannotgo 체크된 곳은 가지 못한다
	4. 최소 거리로 이동 도착까지
	5. 다음 좌표 리턴
	*/
	static int[] findClosePathStore(int midx) {
		int[] storePos = stores[midx];
		Man man = mans[midx];
		
		Deque<int[]> q = new ArrayDeque<>();
		q.offer(new int[] {man.r, man.c, -1}); // -1은 방향
		
		boolean[][] vis = new boolean[N+1][N+1];
		vis[man.r][man.c] = true;
		
		int[] nextPos = new int[]{man.r, man.c};
		int aptDir = -1;
		
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int r = now[0];
			int c = now[1];
			
			if(r == storePos[0] && c == storePos[1]) {
				aptDir = now[2];
				break;
			}
			
			for(int dir=0; dir<4; dir++) {
				int nr = r + DR[dir];
				int nc = c + DC[dir];
				
				if(outOfBorder(nr, nc)) continue;
				if(vis[nr][nc]) continue;
				if(cannotgo[nr][nc]) continue;
				
				// 이동할떄 첫번재 방향 찾기
				int firstDir = now[2];
				if(r == man.r && c == man.c) {
					firstDir = dir;
				}
				
				
				vis[nr][nc] = true;
				q.offer(new int[] {nr, nc, firstDir});
			}
		}
		
		if(aptDir != -1) {
			nextPos = new int[] {man.r + DR[aptDir], man.c + DC[aptDir]};
		}else {
			System.out.println("사람들 이동 방향 찾기 오류");
		}
		return nextPos;
	}
	
	/*
	def moveMans
	1. mans 순회
	1-1. man의 done이라면 이동하지 않는다
	2. findclosepathstore 호출해서 다음 좌표 얻어오기
	3. 좌표 갱신
	4. 편의점이면 done 갱신
	5. cannotgo 갱신
	*/
	static void moveMans() {
		List<Integer> bannedList = new ArrayList<>();
		
		for(int midx=1; midx <= M; midx++) {
			if(!mans[midx].onField) continue;
			if(mans[midx].done) continue; 
			int[] nextPos = findClosePathStore(midx);
			mans[midx].r = nextPos[0];
			mans[midx].c = nextPos[1];
			
			if(mans[midx].r == stores[midx][0] && mans[midx].c == stores[midx][1]) {
				bannedList.add(midx);
			}
		}
		
		for(int banned : bannedList) {
			mans[banned].done = true;
			cannotgo[mans[banned].r][mans[banned].c] = true; 
		}
	}
	
	
	/*
	def gotoCamp: 
	1. mans 순회, 해당 시간 대의 사람이 있는지 확인
	2. 해당 사람은 findcloseCamp를 호출, 캠프 좌표로 갱신한다
	3. cannotgo 갱신
	*/
	static void gotoCamp(int time) {
		for(int midx=1; midx <= M; midx++) {
			if(mans[midx].done) continue; 
			if(mans[midx].time == time) {
				int[] nextPos = findCloseCamp(midx);
				mans[midx].r = nextPos[0];
				mans[midx].c = nextPos[1];
				mans[midx].onField = true;
				cannotgo[nextPos[0]][nextPos[1]] = true;
			}
		}
	}

	/*
	def findCloseCamp:mIdx,
	1. mIdx에 해당하는 편의점 좌표 가져오기
	2. 해당 좌표로부터 bfs, cannotgo 인데는 가지 못한다
	3. board에서 1인 곳(베이스캠프)을 찾는다 
	4. 이때 가장 가까운 캠프 여러개를 찾더라도 해당 캠프랑 거리가 같으면 계속 찾아야 한다
	5. 같으면 행, 열까지 비교 한다
	6. 해당 좌표를 리턴한다
	*/
	private static int[] findCloseCamp(int midx) {
		int[] storePos = stores[midx];
//		System.out.println(midx+"번 사람");
//		System.out.println("store 좌표 :"+storePos[0]+","+storePos[1]);
		Deque<int[]> q = new ArrayDeque<>();
		boolean[][] vis = new boolean[N+1][N+1];
		
		q.offer(new int[]{storePos[0], storePos[1], 0});
		vis[storePos[0]][storePos[1]] = true;
		
		int minDist = N*N;
		int[] minPos = {N+1, N+1};
		
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int r = now[0];
			int c = now[1];
			int dist = now[2];
			
			if(minDist < dist) continue;
			
			if(board[r][c] == 1) {
				if(minDist > dist) {
					// System.out.println("최솟값 갱신"+dist);
					minDist = dist;
					minPos = new int[] {r,c};
				}else if(minDist == dist) {
					if(minPos[0] > r) {
						minPos = new int[]{r, c};
					}else if(minPos[0] == r) {
						if(minPos[1] > c) {
							minPos = new int[] {r,c};
						}
					}
				}
			}
			
			for(int dir=0; dir<4; dir++) {
				int nr = r + DR[dir];
				int nc = c + DC[dir];
				
				if(outOfBorder(nr, nc)) continue;
				if(vis[nr][nc]) continue;
				if(cannotgo[nr][nc]) continue;
				
				vis[nr][nc] = true;
				q.offer(new int[] {nr, nc, now[2] + 1});
			}
		}
//		System.out.println(minPos[0]+"|"+minPos[1]);
		return minPos;
	}
}