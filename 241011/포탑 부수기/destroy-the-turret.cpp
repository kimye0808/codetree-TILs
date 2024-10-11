#include <iostream>
#include <vector>
#include <algorithm>
#include <queue>
#include <tuple>
using namespace std;

struct Potop {
	int x, y;

};

int n, m, k;
vector<vector<int>> table(11, vector<int>(11));
vector<vector<int>> lasta(11, vector<int>(11));

int lz_dx[] = { 0, 1, 0, -1 };
int lz_dy[] = { 1, 0, -1, 0 };
int po_dx[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
int po_dy[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

// i,j가 새로운 애들, r,c가 i,j전까지 attacker 후보
bool p_attacker(int i, int j, int r, int c, int& min_str, int& max_rc, int& max_c) {
	if (r == -1 || c == -1) return true;
	int atk = table[i][j];
	if (atk == 0) return false;

	if (min_str > atk) {
		min_str = atk;
		max_rc = r + c;
		max_c = c;
		return true;
	}
	else if (min_str == atk) {
		if (lasta[r][c] < lasta[i][j]) {
			min_str = atk;
			max_rc = r + c;
			max_c = c;
			return true;
		}
		else if (lasta[r][c] == lasta[i][j]) {
			if (max_rc < r + c) {
				min_str = atk;
				max_rc = r + c;
				max_c = c;
				return true;
			}
			else if (max_rc == r + c) {
				if (max_c < c) {
					min_str = atk;
					max_rc = r + c;
					max_c = c;
					return true;
				}
			}
		}
	}

	return false;
}

pair<int, int> find_attacker() {
	int min_str = 1e9;
	int max_rc = -1;
	int max_c = -1;
	pair<int, int> attacker = make_pair(-1, -1);

	for (int i = 1; i <= n; i++) {
		for (int j = 1; j <= m; j++) {
			//p_attacker
			if (p_attacker(i, j, attacker.first, attacker.second, min_str, max_rc, max_c)) {
				attacker = make_pair(i, j);
			}
		}
	}
	int x = attacker.first;
	int y = attacker.second;

	if (x == -1 || y == -1 || table[x][y] == 0) {
		return make_pair(-1, -1);
	}

	table[x][y] += n + m;
	return attacker;
}

// i,j가 새로운 애들, r,c가 i,j전까지 target 후보
bool p_target(int i, int j, int r, int c, int& max_str, int& min_rc, int& min_c) {
	if (r == -1 || c == -1) return true;
	int atk = table[i][j];
	if (atk == 0) return false;

	if (max_str < atk) {
		max_str = atk;
		min_rc = r + c;
		min_c = c;
		return true;
	}
	else if (max_str == atk) {
		if (lasta[r][c] > lasta[i][j]) {
			max_str = atk;
			min_rc = r + c;
			min_c = c;
			return true;
		}
		else if (lasta[r][c] == lasta[i][j]) {
			if (min_rc < r + c) {
				max_str = atk;
				min_rc = r + c;
				min_c = c;
				return true;
			}
			else if (min_rc == r + c) {
				if (min_c < c) {
					max_str = atk;
					min_rc = r + c;
					min_c = c;
					return true;
				}
			}
		}
	}

	return false;
}

pair<int, int> find_target() {
	int max_str = -1;
	int min_rc = 1e9;
	int min_c = 1e9;
	pair<int, int> target = make_pair(-1, -1);

	for (int i = 1; i <= n; i++) {
		for (int j = 1; j <= m; j++) {
			//p_target
			if (p_target(i, j, target.first, target.second, max_str, min_rc, min_c)) {
				target = make_pair(i, j);
			}
		}
	}
	int x = target.first;
	int y = target.second;

	if (x == -1 || y == -1 || table[x][y] == 0) {
		return make_pair(-1, -1);
	}

	return target;
}

void jungbee(vector<pair<int, int>>& logs) {

	for (int j = 1; j <= n; j++) {
		for (int z = 1; z <= m; z++) {
			if (table[j][z] == 0) continue;

			bool found = false;
			for (int i = 0; i < (int)logs.size(); i++) {
				pair<int, int> log = logs[i];

				int x = log.first;
				int y = log.second;

				if (j == x && z == y) {
					found = true;
					break;
				}
			}

			if (!found) {
				table[j][z] += 1;
			}
		}
	}
}

void dmg_lazer(int ax, int ay, int tx, int ty, vector<pair<int, int>>& logs) {
	int dmg = table[ax][ay];
	for (int i = 0; i < (int)logs.size(); i++) {
		pair<int, int> log = logs[i];

		int x = log.first;
		int y = log.second;

		if (x == tx && y == ty) {
			table[tx][ty] -= dmg;
			if (table[tx][ty] <= 0) {
				table[tx][ty] = 0;
			}
		}
		else {
			if (x == ax && y == ay) continue;
			table[x][y] -= dmg / 2;
			if (table[x][y] <= 0) {
				table[x][y] = 0;
			}
		}
	}

	//정비
	jungbee(logs);
}

bool lazer(int ax, int ay, int tx, int ty) {
	int dist[11][11] = { 0 };
	queue < tuple<int, int, vector<pair<int, int>> > > q;
	vector<pair<int, int>> v;
	v.push_back(make_pair(ax, ay));
	q.push(make_tuple(ax, ay, v));

	while (!q.empty()) {
		tuple<int, int, vector<pair<int, int>>> t = q.front();
		q.pop();

		int x = get<0>(t);
		int y = get<1>(t);
		vector<pair<int, int>> logs = get<2>(t);

		if (x == tx && y == ty) {
			//레이저 처리
			dmg_lazer(ax, ay, tx, ty, logs);
			return true;
		}

		for (int dir = 0; dir < 4; dir++) {
			int nx = x + lz_dx[dir];
			int ny = y + lz_dy[dir];

			if (nx<1 || ny<1 || nx>n || ny>m) {
				//처리
				if (dir == 0) {
					ny = ny % m;
				}
				else if (dir == 1) {
					nx = nx % n;
				}
				else if (dir == 2) {
					ny = ny + m;
				}
				else if (dir == 3) {
					nx = nx + n;
				}
			}
			if (dist[nx][ny]) continue;
			if (table[nx][ny] == 0) continue;

			vector<pair<int, int>> logtmp = logs;
			logtmp.push_back(make_pair(nx, ny));
			dist[nx][ny] = dist[x][y] + 1;

			q.push(make_tuple(nx, ny, logtmp));
		}
	}

	return false;//레이저 실패
}

void wrap(int& x, int& y) {
	if (x < 1) x += n;
	if (x > n) x -= n;
	if (y < 1) y += m;
	if (y > m) y -= m;
}

void potan(int ax, int ay, int tx, int ty) {
	int dmg = table[ax][ay];
	vector<pair<int, int>> logs;
	logs.push_back(make_pair(ax, ay));

	for (int dir = 0; dir < 8; dir++) {
		int nx = tx + po_dx[dir];
		int ny = ty + po_dy[dir];

		wrap(nx, ny);

		if (table[nx][ny] == 0) continue;

		logs.push_back(make_pair(nx, ny));
		table[nx][ny] -= dmg / 2;
		if (table[nx][ny] <= 0) {
			table[nx][ny] = 0;
		}
	}

	table[tx][ty] -= dmg;
	if (table[tx][ty] <= 0) {
		table[tx][ty] = 0;
	}

	jungbee(logs);
}

int main() {
	cin >> n >> m >> k;

	for (int i = 1; i <= n; i++) {
		for (int j = 1; j <= m; j++) {
			cin >> table[i][j];
		}
	}
	//for (int i = 1; i <= n; i++) {
	//	for (int j = 1; j <= m; j++) {
	//		cout << table[i][j] << ' ';
	//	}
	//	cout << '\n';
	//}
	for (int turn = 1; turn <= k; turn++) {
		//find_attacker
		pair<int, int> atk = find_attacker();
		int ax = atk.first;
		int ay = atk.second;
		//if (ax == -1 || ay == -1) cout << "not found attacker!: " << k << '\n';
		//cout << ax << ' ' << ay << '\n';

		//find_target
		pair<int, int> tar = find_target();
		int tx = tar.first;
		int ty = tar.second;
		//if (tx == -1 || ty == -1) cout << "not found target!: " << k << '\n';
		//cout << tx << ' ' << ty << '\n';


		//cout << "before atk" << '\n';
		//for (int i = 1; i <= n; i++) {
		//	for (int j = 1; j <= m; j++) {
		//		cout << table[i][j] << ' ';
		//	}
		//	cout << '\n';
		//}

		//lazer
		if (!lazer(ax, ay, tx, ty)) {
			//potan
			potan(ax, ay, tx, ty);
		}
		//cout << "after atk" << '\n';
		//for (int i = 1; i <= n; i++) {
		//	for (int j = 1; j <= m; j++) {
		//		cout << table[i][j] << ' ';
		//	}
		//	cout << '\n';
		//}

		//last attacker 갱신
		lasta[ax][ay] = turn;
	}

	int maxpo = 0;
	for (int i = 1; i <= n; i++) {
		for (int j = 1; j <= m; j++) {
			if (maxpo < table[i][j]) {
				maxpo = table[i][j];
			}
		}
	}

	cout << maxpo;
}