/*
1. dir 방향 문제랑 일치시키기, dir 여러개면 방향 일치시키기,

2. 문제 조건에 맞게 위쪽부터 1~R 로 설정하는게 덜 헷갈렸을 것 같다
*/

#include <iostream>
#include <vector>
#include <queue>

using namespace std;

const int MAX_SIZE = 75;
const int DIRECTIONS = 4;
const int dx[DIRECTIONS] = { 1, 0, -1, 0 }; // 아래, 오른쪽, 위, 왼쪽
const int dy[DIRECTIONS] = { 0, 1, 0, -1 };

vector<vector<int>> maps(MAX_SIZE, vector<int>(MAX_SIZE, 0));
vector<vector<bool>> exitp(MAX_SIZE, vector<bool>(MAX_SIZE, false));
vector<vector<bool>> vis(MAX_SIZE, vector<bool>(MAX_SIZE, false));

int r, c, k;
int sum = 0;

void reset_g() {
    for (int i = 0; i < MAX_SIZE; i++) {
        fill(exitp[i].begin(), exitp[i].end(), false);
        fill(maps[i].begin(), maps[i].end(), 0);
        fill(vis[i].begin(), vis[i].end(), false);
    }
}

// 골렘 회전 함수
bool rotate_gol(pair<int, int>(&gtmp)[5], int& exitdir, int lr) {
    // 회전 가능 여부 체크 (왼쪽 또는 오른쪽)
    for (int i = 0; i < 5; i++) {
        if ((lr == -1 && i == 1) || (lr == 1 && i == 3)) continue;  // 왼쪽 또는 오른쪽 회전 제외
        int newX = gtmp[i].first;
        int newY = gtmp[i].second + lr;

        if (newX < 1 || newY < 1 || newY > c || vis[newX][newY]) {
            return false;
        }
    }

    // 아래로 이동 가능한지 체크
    for (int i = 0; i < 5; i++) {
        if ((lr == -1 && i == 1) || (lr == 1 && i == 3)) continue;
        int newX = gtmp[i].first - 1;
        int newY = gtmp[i].second + lr;

        if (newX < 1 || newY < 1 || newY > c || vis[newX][newY]) {
            return false;
        }
    }

    // 좌표 갱신
    for (int i = 0; i < 5; i++) {
        gtmp[i].first -= 1;
        gtmp[i].second += lr;
    }

    // 회전 방향에 따라 exitdir 조정
    exitdir = (exitdir + (lr == -1 ? -1 : 1) + DIRECTIONS) % DIRECTIONS;
    return true;
}

// BFS를 사용하여 가장 낮은 위치 탐색
void bfs(int x, int y) {
    queue<pair<int, int>> q;
    q.push(make_pair(x, y));
    vector<vector<bool>> visited(MAX_SIZE, vector<bool>(MAX_SIZE, false));
    visited[x][y] = true;

    int lowRow = r;

    while (!q.empty()) {
        int px = q.front().first;
        int py = q.front().second;
        q.pop();

        for (int dir = 0; dir < DIRECTIONS; dir++) {
            int pnx = px + dx[dir];
            int pny = py + dy[dir];

            if (pnx < 1 || pny < 1 || pnx > r || pny > c || visited[pnx][pny]) continue;
            if (maps[pnx][pny] == 0) continue;

            if (exitp[px][py] || (maps[pnx][pny] == maps[px][py])) {
                q.push(make_pair(pnx, pny));
                visited[pnx][pny] = true;
                lowRow = min(lowRow, pnx);
            }
        }
    }

    sum += r + 1 - lowRow;
}

// 골렘 이동 처리 함수
bool move_gol(int col, int d, int id) {
    pair<int, int> gtmp[5] = {//(위, 오, 아, 왼), 원점
        {1, 0}, {0, 1}, {-1, 0}, {0, -1}, {0, 0}
    };

    int exitdir = d;

    // 시작 좌표 설정
    for (int i = 0; i < 5; i++) {
        gtmp[i].first += r + 2;
        gtmp[i].second += col;
    }

    // 골렘이 아래로 이동하면서 체크
    while (true) {
        int bx = gtmp[2].first - 1;
        if (bx < 1) break;  // 숲을 벗어나면 중단
        bool canMove = true;

        for (int i = 0; i < 5; i++) {
            if (vis[gtmp[i].first - 1][gtmp[i].second]) {
                canMove = false;
                break;
            }
        }

        // 아래로 이동 가능한 경우
        if (canMove) {
            for (int i = 0; i < 5; i++) {
                gtmp[i].first -= 1;
            }
        }
        else {
            // 회전 시도 (왼쪽 우선, 실패하면 오른쪽)
            if (!rotate_gol(gtmp, exitdir, -1)) {
                if (!rotate_gol(gtmp, exitdir, 1)) {
                    break;
                }
            }
        }
    }

    // 골렘이 숲에 다 들어가지 못한 경우
    if (gtmp[0].first > r) {
        // 리셋하기 전에 sum 계산
        return false;
    }

    // vis와 maps 갱신
    for (int i = 0; i < 5; i++) {
        int x = gtmp[i].first;
        int y = gtmp[i].second;
        vis[x][y] = true;
        maps[x][y] = id;
    }
    exitp[gtmp[exitdir].first][gtmp[exitdir].second] = true;


    bfs(gtmp[4].first, gtmp[4].second);
    return true;
}

int main() {
    ios::sync_with_stdio(0); cin.tie(0); cout.tie(0);

    cin >> r >> c >> k;

    for (int id = 1; id <= k; id++) {
        int col, d;
        cin >> col >> d;
        if (!move_gol(col, d, id)) {
            reset_g();
        }
    }

    cout << sum;
    return 0;
}