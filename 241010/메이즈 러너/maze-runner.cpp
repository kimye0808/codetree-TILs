#include <iostream>
#include <vector>
#include <algorithm>
#include <tuple>
using namespace std;

struct Man {
	int x, y;
	bool done;
};

int n, m, k;
int dist_sum;
int exitx, exity;
vector<vector<int>> ptable(11, vector<int>(11));
vector<vector<int>> wtable(11, vector<int>(11));
vector<Man> man(11);

int dx[] = { -1, 1, 0, 0 };
int dy[] = { 0, 0, 1, -1 };

int calc_dist(int fx, int fy, int tx, int ty) {
	int x = fx - tx > 0 ? fx - tx : tx - fx;
	int y = fy - ty > 0 ? fy - ty : ty - fy;
	return x + y;
}

void move() {
	for (int i = 1; i <= m; i++) {
		if (man[i].done) continue;// 이미 종료했다면

		int x = man[i].x;
		int y = man[i].y;

		int now_dist = calc_dist(x, y, exitx, exity);
		int dist = calc_dist(x, y, exitx, exity);
		int after_dir = -1;

		for (int dir = 0; dir < 4; dir++) {
			int nx = x + dx[dir];
			int ny = y + dy[dir];

			if (nx<1 || ny<1 || nx>n || ny>n) continue;
			if (wtable[nx][ny]) continue;

			int after_dist = calc_dist(nx, ny, exitx, exity);

			if (after_dist < dist) {
				dist = after_dist;
				after_dir = dir;
			}
		}

		// 상태 갱신
		if (dist >= now_dist) continue;
		int nx = x + dx[after_dir];
		int ny = y + dy[after_dir];

		bool isdup = false;
		for (int j = i+1; j <= m; j++) {// 한 칸에 사람 여러명 존재시
			if (man[j].x == man[i].x && man[j].y == man[i].y) {
				isdup = true;
				break;
			}
		}
		if (!isdup) {
			ptable[x][y] = 0;
		}

		dist_sum += 1;

		if (ptable[nx][ny] == -1) {//출구 좌표라면
			man[i].done = true;
		}
		else {
			ptable[nx][ny] = i;
			man[i].x = nx;
			man[i].y = ny;
		}
	}
}

tuple<int, int, int> find_square() {// {-1,-1} 리턴할 수도 있음
	int min_side = 20;
	int tx = -1;
	int ty = -1;
	for (int side = 0; side <= n; side++) {
		for (int r = 1; r <= n; r++) {
			for (int c = 1; c <= n; c++) {
				// 정사각형 안에 man과 exit가 있으면
				bool man_found = false;
				bool exit_found = false;
				int maxr = r + side;
				int maxc = c + side;
				if (maxr > n || maxc > n) continue;

				for (int i = r; i <= r + side; i++) {
					for (int j = c; j <= c + side; j++) {
						if (ptable[i][j] > 0) {
							man_found = true;
						}
						else if (ptable[i][j] == -1) {
							exit_found = true;
						}
					}
				}
				if (man_found && exit_found && min_side > side) {
					min_side = side;
					tx = r;
					ty = c;
				}
			}
		}
	}

	return make_tuple(tx, ty, min_side);
}

void rotation(int x, int y, int side) {
	vector<vector<int>> tmp_table = ptable;
	vector<vector<int>> tmp_wtable = wtable;

	for (int r = x; r <= x + side; r++) {
		for (int c = y; c <= y + side; c++) {
			if (wtable[r][c] > 0) {
				wtable[r][c] -= 1;
			}
		}
	}

	for (int r = x; r <= x + side; r++) {
		for (int c = y; c <= y + side; c++) {
			int ox = r - x;
			int oy = c - y;
			int prevx = -(oy - side) + x;
			int prevy = ox + y;

			int newx = ox + x;
			int newy = oy + y;
			tmp_table[newx][newy] = ptable[prevx][prevy];
			tmp_wtable[newx][newy] = wtable[prevx][prevy];

			int id = tmp_table[newx][newy];
			if (id > 0) {// 사람 좌표 갱신
				man[id].x = newx;
				man[id].y = newy;
			}
			else if (id == -1) {
				exitx = newx;
				exity = newy;
			}
		}
	}

	ptable = tmp_table;
	wtable = tmp_wtable;
}

int main() {
	cin >> n >> m >> k;

	for (int i = 1; i <= n; i++) {
		for (int j = 1; j <= n; j++) {
			int tmp;
			cin >> tmp;
			if (tmp > 0) {
				wtable[i][j] = tmp;
			}
		}
	}

	for (int i = 1; i <= m; i++) {
		int x, y;
		cin >> x >> y;

		man[i].x = x;
		man[i].y = y;
		ptable[x][y] = i;
	}

	cin >> exitx >> exity;
	ptable[exitx][exity] = -1;

	while (k--) {
		//cout << "mvoe 전" << '\n';
		//for (int i = 1; i <= n; i++) {
		//	for (int j = 1; j <= n; j++) {
		//		cout << ptable[i][j] << ' ';
		//	}
		//	cout << '\n';
		//}
		//cout << '\n';
		
		//move
		move();
		//cout << "move 이후" << '\n';
		//for (int i = 1; i <= n; i++) {
		//	for (int j = 1; j <= n; j++) {
		//		cout << ptable[i][j] << ' ';
		//	}
		//	cout << '\n';
		//}
		//cout << '\n';
		
		//find_square
		tuple<int, int, int> t = find_square();
		int x = get<0>(t);
		int y = get<1>(t);
		int s = get<2>(t);
		if (x == -1 && y == -1) {
			break;
		}

		//cout << "벽 회전 전:" << '\n';
		//for (int i = 1; i <= n; i++) {
		//	for (int j = 1; j <= n; j++) {
		//		cout << wtable[i][j] << ' ';
		//	}
		//	cout << '\n';
		//}
		// 
		
		//rotation
		rotation(x, y, s);
		
		//cout << "rotation 이후" << '\n';
		//cout << k << " 턴 종료: " << '\n';
		//for (int i = 1; i <= n; i++) {
		//	for (int j = 1; j <= n; j++) {
		//		cout << ptable[i][j] << ' ';
		//	}
		//	cout << '\n';
		//}
		//cout << '\n';
		//for (int i = 1; i <= n; i++) {
		//	for (int j = 1; j <= n; j++) {
		//		cout << wtable[i][j] << ' ';
		//	}
		//	cout << '\n';
		//}
	}

	cout << dist_sum << '\n';
	cout << exitx << ' ' << exity;
}