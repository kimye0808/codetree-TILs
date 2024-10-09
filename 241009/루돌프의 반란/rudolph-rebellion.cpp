#include <iostream>
#include <climits>
#include <vector>
#include <algorithm>
using namespace std;

struct San {
	int x, y;
	int score;
	int faint;
	bool onboard;
};

struct Ru {
	int x, y;
};

/*
N: 게임판의 크기(3≤N≤50)
M : 게임 턴 수(1≤M≤1000)
P : 산타의 수(1≤P≤30)
C : 루돌프의 힘(1≤C≤N)
D : 산타의 힘(1≤D≤N)
*/

int n, m, p, c, d;
Ru ru;
vector<San> san(31);
vector<vector<int>> board(51, vector<int>(51, 0));

int rdx[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
int rdy[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

int sdx[] = { -1, 0, 1, 0 };
int sdy[] = { 0, 1, 0, -1 };

bool inrange(int x, int y) {
	bool result = true;
	if (x<1 || y<1 || x>n || y>n) {
		result = false;
	}
	return result;
}

void print_table() {
	for (int i = 1; i <= n; i++) {
		for (int j = 1; j <= n; j++) {
			cout << board[i][j] << ' ';
		}
		cout << '\n';
	}
}

/*
* id: 들어오는 산타 id
* x: 해당 x 좌표
* y: 해당 y 좌표
* dir: 움직이는 dir 방향
*/
void r_sangho(int id, int x, int y, int dir) {
	if (!inrange(x, y)) return;
	if (board[x][y] == 0) {
		board[x][y] = id;
		san[id].x = x;
		san[id].y = y;
		return;
	}
	else if (board[x][y] > 0) {
		int coming_id = id;
		int present_id = board[x][y];

		int nx = x + rdx[dir];
		int ny = y + rdy[dir];

		r_sangho(present_id, nx, ny, dir);

		board[x][y] = coming_id;
		san[coming_id].x = x;
		san[coming_id].y = y;
	}
}

/*
* id: 들어오는 산타 id
* x: 해당 x 좌표
* y: 해당 y 좌표
* dir: 움직이는 dir 방향
*/
void s_sangho(int id, int x, int y, int dir) {
	if (!inrange(x, y)) return;
	if (board[x][y] == 0) {
		board[x][y] = id;
		san[id].x = x;
		san[id].y = y;
		return;
	}
	else if (board[x][y] > 0) {
		int coming_id = id;
		int present_id = board[x][y];

		int nx = x + sdx[dir];
		int ny = y + sdy[dir];

		s_sangho(present_id, nx, ny, dir);

		board[x][y] = coming_id;
		san[coming_id].x = x;
		san[coming_id].y = y;
	}
}

/*
* id: 들어오는 id
* x: 해당 x, y: 해당 y
* dir: 들어오는 dir
*/
bool r_choongdol(int turn, int id, int x, int y, int dir) {
	int sid = board[x][y];//충돌한 곳에 있는 id
	san[sid].score += c;
	san[sid].faint = turn;

	//sangho
	int nx = x + c * rdx[dir];
	int ny = y + c * rdy[dir];
	if (!inrange(nx, ny)) {
		san[sid].onboard = false;
		return false;
	}
	if (board[nx][ny] > 0) {//밀려났는데도 산타 있을시
		r_sangho(sid, nx, ny, dir);
	}
	else {
		board[nx][ny] = sid;
		san[sid].x = nx;
		san[sid].y = ny;
	}
	return true;
}
/*
* id: 들어오는 id
* x: 해당 x, y: 해당 y
* dir: 들어오는 dir
*/
bool s_choongdol(int turn, int id, int x, int y, int dir) {
	san[id].score += d;
	san[id].faint = turn;

	int ch_dir = dir - 2 >= 0 ? dir - 2 : dir + 2;
	int nx = x + d * sdx[ch_dir];
	int ny = y + d * sdy[ch_dir];

	if (!inrange(nx, ny)) {
		san[id].onboard = false;
		return false;//false면 범위 밖이므로 원래 좌표 0으로
	}
	if (board[nx][ny] > 0) {//밀려났는데도 산타 있을시
		s_sangho(id, nx, ny, ch_dir);
	}
	else {
		board[nx][ny] = id;
		san[id].x = nx;
		san[id].y = ny;
	}
	return true;
}


int calc_dist(int fx, int fy, int tx, int ty) {
	int dist = ((fx - tx) * (fx - tx)) + ((fy - ty) * (fy - ty));
	return dist;
}

int find_close_santa() {
	int rx = ru.x;
	int ry = ru.y;

	//cout << "rx: " << rx << " ry: " << ry << '\n';
	int min_san = -1;
	int mind = INT_MAX;
	for (int i = n; i >= 1; i--) {//우선순위 처리 
		for (int j = n; j >= 1; j--) {
			if (board[i][j] > 0) {
				int sid = board[i][j];

				int dist = calc_dist(rx, ry, san[sid].x, san[sid].y);

				//cout << sid << ' ' << "x: " << san[sid].x << " y: " << san[sid].y << ' ' << dist << '\n';
				if (mind > dist) {
					mind = dist;
					min_san = sid;
				}
			}
		}
	}
	return min_san;
}

int find_close_dir(int sid) {
	int x = ru.x;
	int y = ru.y;

	int min_dir = -1;
	int mind = INT_MAX;
	for (int dir = 0; dir < 8; dir++) {
		int nx = x + rdx[dir];
		int ny = y + rdy[dir];

		if (!inrange(nx, ny)) continue;

		int dist = calc_dist(nx, ny, san[sid].x, san[sid].y);

		if (mind > dist) {
			mind = dist;
			min_dir = dir;
		}
	}

	return min_dir;
}

bool move_ru(int turn) {
	int sid = find_close_santa();
	if (sid == -1) {//가까운 산타를 몾찾음
		return false;//종료
	}


	int close_dir = find_close_dir(sid);
	if (close_dir == -1) {
		return false;
	}

	int nx = ru.x + rdx[close_dir];
	int ny = ru.y + rdy[close_dir];

	if (board[nx][ny] > 0) {//산타가 있다면
		board[ru.x][ru.y] = 0;
		if (!r_choongdol(turn, -1, nx, ny, close_dir)) {
			board[nx][ny] = -1;
			ru.x = nx;
			ru.y = ny;
		}
		board[nx][ny] = -1;
		ru.x = nx;
		ru.y = ny;
	}
	else {
		board[nx][ny] = -1;
		board[ru.x][ru.y] = 0;
		ru.x = nx;
		ru.y = ny;
	}
	return true;
}


int get_san_dir(int sid) {
	int x = san[sid].x;
	int y = san[sid].y;

	int min_dir = -1;
	int mind = calc_dist(x,y, ru.x, ru.y);
	for (int dir = 0; dir < 4; dir++) {
		int nx = x + sdx[dir];
		int ny = y + sdy[dir];

		if (!inrange(nx, ny)) continue;
		if (board[nx][ny] > 0) continue;

		int dist = calc_dist(nx, ny, ru.x, ru.y);

		if (mind > dist) {
			mind = dist;
			min_dir = dir;
		}
	}

	return min_dir;
}

void move_san(int turn, int sid) {
	int close_dir = get_san_dir(sid);
	if (close_dir == -1) {
		return;
	}

	int nx = san[sid].x + sdx[close_dir];
	int ny = san[sid].y + sdy[close_dir];

	if (board[nx][ny] == -1) {//산타->루돌프 충돌
		board[san[sid].x][san[sid].y] = 0;
		s_choongdol(turn, sid, nx, ny, close_dir);
	}
	else {
		board[nx][ny] = sid;
		board[san[sid].x][san[sid].y] = 0;
		san[sid].x = nx;
		san[sid].y = ny;
	}

}

int main() {
	cin >> n >> m >> p >> c >> d;

	cin >> ru.x >> ru.y;
	board[ru.x][ru.y] = -1;

	for (int i = 1; i <= p; i++) {
		int sid, x, y;
		cin >> sid >> x >> y;
		san[sid].x = x;
		san[sid].y = y;
		san[sid].onboard = true;
		board[x][y] = sid;
	}
	
	/*
		cout << "initial" << '\n';
		print_table();
		cout << '\n';
	*/



	for (int turn = 1; turn <= m; turn++) {
		move_ru(turn);

		/*
		cout << turn << "턴 ru 종료 이후" << '\n';
		print_table();
		cout << '\n';
		*/


		for (int i = 1; i <= p; i++) {
			if (san[i].faint != 0 && san[i].faint + 2 > turn) {
				//cout << "faint" << '\n';
				//cout << i << '\n';
				//cout << san[i].faint << '\n';
				continue;
			}
			if (!san[i].onboard) continue;

			move_san(turn, i);

			//cout << turn << "턴 san " << i << "종료 이후" << '\n';
			//print_table();
			//cout << '\n';
		}

		//종료 처리
		int failed_cnt = 0;
		for (int i = 1; i <= p; i++) {
			if (san[i].onboard == false) {
				failed_cnt++;
				continue;
			}
		}
		if (failed_cnt == p) {
			break;
		}

		//점수 처리
		for (int i = 1; i <= p; i++) {
			if (san[i].onboard == false) {
				continue;
			}
			san[i].score++;
		}

		//cout << turn << "턴 종료 이후" << '\n';
		//print_table();
		//cout << '\n';
	}

	for (int i = 1; i <= p; i++) {
		cout << san[i].score << ' ';
	}
}