/*
 * @lc app=leetcode.cn id=122 lang=java
 *
 * [122] 买卖股票的最佳时机 II
 */

// @lc code=start
class Solution {
    public int maxProfit(int[] prices) {        
        if (null == prices || prices.length <= 1) return 0;
        int profit = 0;
        int tmp = 0;
        for (int i = 1; i<prices.length; i++ ) {
            tmp = prices[i] - prices[i-1];
            if (tmp>0) profit = profit + tmp;
        }
        return profit;
        
    }
}
// @lc code=end

