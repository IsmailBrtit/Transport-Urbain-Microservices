package testEffyis;

import java.util.Arrays;

public class Solution {

    public int[] productExceptSelf(int[] nums) {
        int[] pre = new int[nums.length];
        int[] post = new int[nums.length];
        for (int i=0;i<nums.length;i++) {
            if(i==0){
                pre[i]=nums[i];
            }
            else{
                pre[i]=nums[i]*pre[i-1];
            }
        }
        System.out.println("pre:"+Arrays.toString(pre));
        for (int i=nums.length-1 ; i>=0 ; i--) {
//            System.out.println(i);
            if(i==nums.length-1){
                post[i] = nums[i];
            }
            else {
                post[i] = nums[i] * post[i + 1];
            }

        }
        System.out.println("post:"+Arrays.toString(post));
        int[] res =  new int[nums.length];
        for(int i=0; i<nums.length;i++){
            if(i == 0){
                res[i]=post[i+1];
            } else if (i== nums.length-1) {
                res[i]=pre[i-1];
            }else {
                res[i] = pre[i-1]*post[i+1];
            }

        }
//        System.out.println("res:"+Arrays.toString(res));
        return res;
    }

    public static void main(String[] args) {
        Solution s = new Solution();
        int[] res = s.productExceptSelf(new int[]{1,2,3,4,5});
        System.out.println("res:"+Arrays.toString(res));
    }
}
