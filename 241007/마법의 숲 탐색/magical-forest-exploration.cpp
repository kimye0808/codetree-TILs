#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int dx[4] = { 1, 0, -1, 0 };
int dy[4] = { 0, 1, 0, -1 };

pair<int, int> gol[5] = {
    {0, 0},  // 원점
    {0, 1},  // 오른쪽
    {-1, 0}, // 아랫쪽
    {0, -1}, // 왼쪽
    {1, 0}   // 윗쪽
};

// 골렘 회전 함수
bool rotate_gol(pair<int, int>(&gtmp)[5], vector<vector<bool>>& vis, int& exitdir, int lr, int r, int c) {
    // 왼쪽 또는 오른쪽으로 회전하는 경우
    for (int i = 0; i < 5; i++) {
        int lrx = gtmp[i].first;
        int lry = gtmp[i].second + lr;

        if (lry < 1 || lry > c || vis[lrx][lry]) {
            return false;
        }
    }

    // 아래로 이동 가능한지 체크
    for (int i = 0; i < 5; i++) {
        int lrx = gtmp[i].first - 1;
        int lry = gtmp[i].second + lr;

        if (lrx < 1 || lry < 1 || lry > c || vis[lrx][lry]) {
            return false;
        }
    }

    // 좌표 갱신
    for (int i = 0; i < 5; i++) {
        gtmp[i].first -= 1;
        gtmp[i].second += lr;
    }

    // 회전 방향에 따라 exitdir 조정
    exitdir = (exitdir + (lr == -1 ? -1 : 1) + 4) % 4;
    return true;
}

// 골렘 이동 처리 함수
bool move_gol(vector<vector<int>>& maps, vector<vector<bool>>& vis, int& sum, int r, int c, int col, int d) {
    pair<int, int> gtmp[5] = { {0, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 0} };
    int exitdir = d;

    // 시작 좌표 설정
    for (int i = 0; i < 5; i++) {
        gtmp[i].first = gol[i].first + r + 2;
        gtmp[i].second = gol[i].second + col;
    }

    // 골렘이 아래로 이동하면서 체크
    while (true) {
        int bx = gtmp[2].first - 1;
        if (bx < 1) break;  // 숲을 벗어나면 중단

        bool canMove = !vis[gtmp[1].first - 1][gtmp[1].second] &&
            !vis[gtmp[2].first - 1][gtmp[2].second] &&
            !vis[gtmp[3].first - 1][gtmp[3].second];

        // 아래로 이동 가능한 경우
        if (canMove) {
            for (int i = 0; i < 5; i++) {
                gtmp[i].first -= 1;
            }
        }
        else {
            // 회전 시도 (왼쪽 우선, 실패하면 오른쪽)
            if (!rotate_gol(gtmp, vis, exitdir, -1, r, c)) {
                if (!rotate_gol(gtmp, vis, exitdir, 1, r, c)) {
                    break;
                }
            }
        }
    }

    // 골렘이 숲에 다 들어가지 못한 경우
    if (gtmp[4].first > r) {
        return false;
    }

    // 블록 최하단 좌표 갱신
    int lowr = gtmp[2].first;
    for (int i = 0; i < 4; i++) {
        int nx = gtmp[exitdir].first + dx[i];
        int ny = gtmp[exitdir].second + dy[i];
        if (nx >= 1 && ny >= 1 && nx <= r && ny <= c && vis[nx][ny]) {
            lowr = min(lowr, maps[nx][ny]);
        }
    }

    // vis와 maps 갱신
    for (int i = 0; i < 5; i++) {
        int x = gtmp[i].first;
        int y = gtmp[i].second;
        vis[x][y] = true;
        maps[x][y] = lowr;
    }

    sum += r + 1 - lowr;
    return true;
}

int main() {
    ios::sync_with_stdio(0); cin.tie(0); cout.tie(0);
    vector<vector<int>> maps(100, vector<int>(100, 0));
    vector<vector<bool>> vis(100, vector<bool>(100, false));

    int r, c, k;
    cin >> r >> c >> k;
    int sum = 0;

    for (int i = 0; i < k; i++) {
        int col, d;
        cin >> col >> d;
        if (!move_gol(maps, vis, sum, r, c, col, d)) {
            fill(maps.begin(), maps.end(), vector<int>(100, 0));
            fill(vis.begin(), vis.end(), vector<bool>(100, false));
        }
    }

    cout << sum;
    return 0;
}