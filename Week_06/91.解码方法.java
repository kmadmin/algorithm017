/*
 * @lc app=leetcode.cn id=91 lang=java
 *
 * [91] 解码方法
 */

// @lc code=start
class Solution {
    public int numDecodings(String s) {
        if(s.charAt(0)=='0'){
            return 0;
        }
        int [] dp = new int [s.length()+1];
        dp[0] = 1;
        dp[1] = 1;
        if(s.length()==1)   return dp[1];
        for(int i=2;i<=s.length();i++){
            int num = Integer.valueOf(String.valueOf(s.charAt(i-1)));
            int nums2 = Integer.valueOf(String.valueOf(s.charAt(i-2)));
            if (nums2+num==0||(num==0&&nums2>2)){
                return 0;
            }else if(num==0||nums2==0){
                dp[i] = num==0?dp[i-2]:dp[i-1];
            }else{
                dp[i] = nums2*10+num>26?dp[i-1]:dp[i-2]+dp[i-1];
            }

        }
        return dp[s.length()];
    }
}
// @lc code=end

