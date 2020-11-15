/*
 * @lc app=leetcode.cn id=70 lang=java
 *
 * [70] 爬楼梯
 */

// @lc code=start
class Solution {
    public int climbStairs(int n) {
        if ( 1 == n ) {
            return 1;
        }
        int first = 1;
        int second = 2;
        int third = 0;
        for ( int i=3; i<=n; i++) {
            third = first + second;
            first = second;
            second = third;
        }
        return second;
    }
}
// @lc code=end

