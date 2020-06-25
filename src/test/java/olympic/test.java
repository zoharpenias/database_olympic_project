
package olympic;

import olympic.business.Athlete;
import olympic.business.ReturnValue;
import olympic.business.Sport;
import org.junit.Test;

import java.util.ArrayList;

import static olympic.business.ReturnValue.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class test extends AbstractTest {
    @Test
    public void getCloseAthletesShortTest(){
        /*
        test stages:
            step 1: add 4 valid athlete and 3 sports and make 1,2 take place in sport 10, athlete 3 in 10,20 and 4 in sport 10,20,30.
            step 2: make friends (1,2) (1,3) (3,4) (2,4)
            step 3: athlete 1 should get 2,3 as close, and 4 should have 3 as close.
        */

        System.out.println("getCloseAthletesShortTest...");
        System.out.println("step 1");
        Sport s1 = new Sport();
        s1.setId(10);
        s1.setName("Basketball");
        s1.setCity("Tel Aviv");
        Solution.addSport(s1);
        Sport s2 = new Sport();
        s2.setId(20);
        s2.setName("Soccer");
        s2.setCity("Haifa");
        Solution.addSport(s2);
        Sport s3 = new Sport();
        s3.setId(30);
        s3.setName("Tennis");
        s3.setCity("Ramat HaSharon");
        Solution.addSport(s3);
        for(int i = 1; i < 5; i++){
            Athlete a1 = new Athlete();
            a1.setId(i);
            a1.setName("Avishay");
            a1.setCountry("Herzliya");
            a1.setIsActive(true);
            Solution.addAthlete(a1);
        }
        Solution.athleteJoinSport(10,1);
        Solution.athleteJoinSport(10,2);
        Solution.athleteJoinSport(10,3);
        Solution.athleteJoinSport(10,4);
        Solution.athleteJoinSport(20,3);
        Solution.athleteJoinSport(20,4);
        Solution.athleteJoinSport(30,4);

        System.out.println("step 2");
        Solution.makeFriends(1,2);
        Solution.makeFriends(1,3);
        Solution.makeFriends(2,4);
        Solution.makeFriends(3,4);

        System.out.println("step 3");

        ArrayList<Integer> exp_1 = new ArrayList<Integer>();
        ArrayList<Integer> exp_4 = new ArrayList<Integer>();
        ArrayList<Integer> real_1 = new ArrayList<Integer>();
        ArrayList<Integer> real_4 = new ArrayList<Integer>();

        exp_1.add(2);
        exp_1.add(3);
        exp_4.add(3);
        real_1 = Solution.getCloseAthletes(1);
        real_4 = Solution.getCloseAthletes(4);
        assertEquals(exp_1.size(),real_1.size());
        assertEquals(exp_4.size(),real_4.size());
        assertEquals(real_1.get(0) == 2 && real_1.get(1) == 3, true);
        assertEquals(real_4.get(0) == 3, true);

        System.out.println("getCloseAthletesShortTest passed :)");
    }

    @Test
    public void getCloseAthletesShortTest(){
        /*
        test stages:
            step 1: add 15 valid athlete and 15 sports and make eact athlete i to take place in [1,i] sport. make all the athletes
            friends.
            step 2: athlete 1 friend are:{2,3,4,5,6,7},athlete 15 friends are [1,14],
            athlete 9 friends are {[1,8]} U {10,11}
            step 3: verify the above

        */

        System.out.println("getCloseAthletesLongTest...");
        System.out.println("step 1");
        for(int i = 1; i <=15; i++){
            Sport s1 = new Sport();
            s1.setId(10*i);
            s1.setName("Basketball");
            s1.setCity("Tel Aviv");
            Solution.addSport(s1);
        }

        for(int i = 1; i <= 15; i++){
            Athlete a1 = new Athlete(); // todo: new was missing
            a1.setId(i);
            a1.setName("Avishay");
            a1.setCountry("Herzliya");
            a1.setIsActive(true);
            Solution.addAthlete(a1);
            for(int j = i; j <=15; j++){
                Solution.athleteJoinSport(10*j,i);
            }
        }

        for(int i = 1; i <=15; i++){
            for(int j = i+1; j <=15; j+=2){
                Solution.makeFriends(i,j);
            }

        }

        System.out.println("step 2");

        ArrayList<Integer> exp_1 = new ArrayList<Integer>();
        ArrayList<Integer> exp_9 = new ArrayList<Integer>();
        ArrayList<Integer> exp_15 = new ArrayList<Integer>();
        ArrayList<Integer> real_1 = new ArrayList<Integer>();
        ArrayList<Integer> real_9 = new ArrayList<Integer>();
        ArrayList<Integer> real_15 = new ArrayList<Integer>();

        exp_1.add(2);
        exp_1.add(3);
        exp_1.add(4);
        exp_1.add(5);
        exp_1.add(6);
        exp_1.add(7);

        exp_9.add(1);
        exp_9.add(2);
        exp_9.add(3);
        exp_9.add(4);
        exp_9.add(5);
        exp_9.add(6);
        exp_9.add(7);
        exp_9.add(8);
        exp_9.add(10);
        exp_9.add(11);

        for(int i = 1; i < 15; i++){
            exp_15.add(i);
        }

        real_1 = Solution.getCloseAthletes(1);
        real_9 = Solution.getCloseAthletes(9);
        real_15 = Solution.getCloseAthletes(15);

        assertEquals(exp_1.size(),real_1.size());
        assertEquals(exp_9.size(),real_9.size());
        assertEquals(exp_15.size(),real_15.size());

        for(int i = 0; i < real_1.size(); i++){
            assertEquals(real_1.get(i),exp_1.get(i));
        }

        for(int i = 0; i < real_9.size(); i++){
            assertEquals(real_9.get(i),exp_9.get(i));
        }

        for(int i = 0; i < real_15.size(); i++){
            assertEquals(real_15.get(i),exp_15.get(i));
        }

        System.out.println("getCloseAthletesLongTest passed :)");
        System.out.println("this one was long and hard ;)");
    }

    @Test
    public void getSportsRecommendationTest(){
        /*
        test stages:
            step 1: add 15 valid athlete and 15 sports and make eact athlete i to take place in [1,i] sport. make all the athletes
            friends.
            make athlete 16, with no friends.
            step 2: the recommendation need to be return from athlete 1 is empty
            the recommendation need to be return from athlete 2 is: {10}
            the recommendation need to be return from athlete 15 is:{140,130,120}
            the recommendation need to be return from athlete 16 is empty
            step 3: verify the above
        */
        System.out.println("getSportsRecommendationTest...");
        System.out.println("step 1");
        for(int i = 1; i <=15; i++){
            Sport s1 = new Sport();
            s1.setId(10*i);
            s1.setName("Basketball");
            s1.setCity("Tel Aviv");
            Solution.addSport(s1);
        }

        for(int i = 1; i <= 16; i++){
            Athlete a1 = new Athlete();//todo new was missing
            a1.setId(i);
            a1.setName("Avishay");
            a1.setCountry("Herzliya");
            a1.setIsActive(true);
            Solution.addAthlete(a1);
            for(int j = i; j <=15; j++){
                Solution.athleteJoinSport(10*j,i);
            }
        }

        for(int i = 1; i <=15; i++){
            for(int j = i+1; j <=15; j++){
                Solution.makeFriends(i,j);
            }

        }

        System.out.println("step 2");
        ArrayList<Integer> exp_1 = new ArrayList<Integer>();
        ArrayList<Integer> exp_2 = new ArrayList<Integer>();
        ArrayList<Integer> exp_15 = new ArrayList<Integer>();
        ArrayList<Integer> exp_16 = new ArrayList<Integer>();
        ArrayList<Integer> real_1 = new ArrayList<Integer>();
        ArrayList<Integer> real_2 = new ArrayList<Integer>();
        ArrayList<Integer> real_15 = new ArrayList<Integer>();
        ArrayList<Integer> real_16 = new ArrayList<Integer>();

        exp_2.add(10);
        exp_15.add(120);
        exp_15.add(130);
        exp_15.add(140);

        System.out.println("step 3");

        real_1 = Solution.getSportsRecommendation(1);
        real_2 = Solution.getSportsRecommendation(2);
        real_15 = Solution.getSportsRecommendation(15);
        real_16 = Solution.getSportsRecommendation(16);

        assertEquals(exp_1.size(),real_1.size());
        assertEquals(exp_2.size(),real_2.size());
        assertEquals(exp_15.size(),real_15.size());
        assertEquals(exp_16.size(),real_16.size());


        for(int i = 0; i < real_2.size(); i++){
            assertEquals(real_2.get(i),exp_2.get(i));
        }

        for(int i = 0; i < real_15.size(); i++){
            assertEquals(real_15.get(i),exp_15.get(i));
        }

        System.out.println("getSportsRecommendationTest passed");
        System.out.println("now we can submit it!");
    }


}

