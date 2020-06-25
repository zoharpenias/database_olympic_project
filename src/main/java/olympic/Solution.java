package olympic;

import olympic.business.Athlete;
import olympic.business.ReturnValue;
import olympic.business.Sport;
import olympic.data.DBConnector;
import olympic.data.PostgreSQLErrorCodes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static olympic.business.ReturnValue.*;

public class Solution {

    private static ReturnValue convert (SQLException e){
        if(Integer.valueOf(e.getSQLState()) == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue())
            return ReturnValue.BAD_PARAMS;
        if(Integer.valueOf(e.getSQLState()) == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
            return ReturnValue.ALREADY_EXISTS;
        if(Integer.valueOf(e.getSQLState()) == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue())
            return ReturnValue.BAD_PARAMS;
        if(Integer.valueOf(e.getSQLState()) == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
            return ReturnValue.NOT_EXISTS;
        else
            return ReturnValue.ERROR;
    }

    private static Athlete resultToAthlete(ResultSet res) {
        Athlete athlete = new Athlete();
        try {
            if (!res.next())
                return Athlete.badAthlete();
            athlete.setId(res.getInt(1));
            athlete.setName(res.getString(2));
            athlete.setCountry(res.getString(3));
            athlete.setIsActive(res.getBoolean(4));
        }
        catch (SQLException e) {
            return Athlete.badAthlete();
        }
        return athlete;
    }

    private static Sport resultToSport(ResultSet res) {
        Sport sport = new Sport();
        try {
            if (!res.next()) return Sport.badSport();
            sport.setId(res.getInt(1));
            sport.setName(res.getString(2));
            sport.setCity(res.getString(3));
            sport.setAthletesCount(res.getInt(4));
        }
        catch (SQLException e) {
            return Sport.badSport();
        }
        return sport;
    }

    private static void createAthletesTable() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("CREATE TABLE Athletes (" +
                    "Athlete_Id integer , " +
                    "Athlete_Name text NOT NULL, " +
                    "Country text NOT NULL, " +
                    "Active boolean NOT NULL, " +
                    "PRIMARY KEY (Athlete_Id), " +
                    "CHECK (Athlete_Id > 0));");
            pstmt.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createSportsTable() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("CREATE TABLE Sports (" +
                    "Sport_Id integer ,\n" +
                    "Sport_Name text NOT NULL,\n" +
                    "City text NOT NULL,\n" +
                    "Athletes_Counter integer DEFAULT 0,\n" +
                    "PRIMARY KEY (Sport_Id),\n" +
                    "CHECK (Sport_Id > 0),\n" +
                    "CHECK (Athletes_Counter >= 0));");
            pstmt.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createInvolvedTable() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("CREATE TABLE Involved (" +
                    "Sport_Id integer ," +
                    "Athlete_Id integer ," +
                    "Payment integer DEFAULT 0," +
                    "Ranking integer," +
                    "FOREIGN KEY (Sport_Id) REFERENCES Sports(Sport_Id) ON DELETE CASCADE," +
                    "FOREIGN KEY (Athlete_Id) REFERENCES Athletes(Athlete_Id) ON DELETE CASCADE," +
                    "CHECK (Payment >= 0)," +
                    "CHECK (RANKING >= 1 AND RANKING <=3));");
            pstmt.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createFriendshipsTable() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("CREATE TABLE Friendships (" +
                    "Athlete_Id1 integer ," +
                    "Athlete_Id2 integer ," +
                    "FOREIGN KEY (Athlete_Id1) REFERENCES Athletes(Athlete_Id) ON DELETE CASCADE," +
                    "FOREIGN KEY (Athlete_Id2) REFERENCES Athletes(Athlete_Id) ON DELETE CASCADE," +
                    "CHECK (Athlete_id1 != Athlete_Id2));");
            pstmt.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createViews(){
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(" CREATE VIEW v1 AS SELECT Athletes.Country,Involved.Ranking\n"+
                            "FROM Involved JOIN Athletes ON Involved.Athlete_Id = Athletes.Athlete_Id\n" +
                    " WHERE Involved.Ranking = 1 OR Involved.Ranking = 2 OR Involved.Ranking = 3");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE VIEW CombineAthletesInvolved AS " +
                    "SELECT Athletes.Athlete_Id, Sport_Id, Ranking " +
                    "FROM Athletes JOIN Involved ON Athletes.Athlete_Id = Involved.Athlete_Id;");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE VIEW GetPoints AS \n" +
                    "SELECT Athlete_Id, count(Sport_Id)*3 AS Points\n" +
                    "FROM CombineAthletesInvolved\n" +
                    "WHERE Ranking=1\n" +
                    "GROUP BY Athlete_Id\n" +
                    "UNION\n" +
                    "SELECT Athlete_Id, count(Sport_Id)*2 AS Points \n" +
                    "FROM CombineAthletesInvolved\n" +
                    "WHERE Ranking=2\n" +
                    "GROUP BY Athlete_Id\n" +
                    "UNION \n" +
                    "SELECT Athlete_Id, count(Sport_Id) AS Points \n" +
                    "FROM CombineAthletesInvolved \n" +
                    "WHERE Ranking=3 \n" +
                    "GROUP BY Athlete_Id\n" +
                    "UNION\n" +
                    "SELECT Athlete_Id, count(Sport_Id)*0 AS Points \n" +
                    "FROM CombineAthletesInvolved \n" +
                    "WHERE Ranking is null\n" +
                    "GROUP BY Athlete_Id;");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE VIEW GetMedalsAthlete AS " +
                    "SELECT * FROM Involved WHERE Ranking = 1 OR Ranking = 2 OR Ranking =3");
            pstmt.execute();
            pstmt = connection.prepareStatement("Create view  AthletesPercity AS\n" +
                    "select city, sum(athletes_counter) as numAthletes from sports\n" +
                    "group by city");
            pstmt.execute();
            pstmt = connection.prepareStatement("create view  sportspercity AS\n" +
                    "select city, count(sport_id) as numSports from sports\n" +
                    "group by city");
            pstmt.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static void createTables() {
        createAthletesTable();
        createSportsTable();
        createInvolvedTable();
        createFriendshipsTable();
        createViews();
    }

    public static void clearTables() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM Athletes");
            pstmt.execute();
            pstmt = connection.prepareStatement("DELETE FROM Sports");
            pstmt.execute();
            pstmt = connection.prepareStatement("DELETE FROM Involved");
            pstmt.execute();
            pstmt = connection.prepareStatement("DELETE FROM Friendships");
            pstmt.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void dropTables() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS Athletes CASCADE ");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS Sports CASCADE");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS Involved CASCADE");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS Friendships CASCADE");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP VIEW IF EXISTS v1");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP VIEW IF EXISTS CombineAthletesInvolved");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP VIEW IF EXISTS GetPoints");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP VIEW IF EXISTS GetMedalsAthlete");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP VIEW IF EXISTS AthletesPercity");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP VIEW IF EXISTS sportspercity");
            pstmt.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ReturnValue addAthlete(Athlete athlete) {
        if (athlete == null)
            return ReturnValue.BAD_PARAMS;
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("INSERT INTO Athletes" +
                    " VALUES (?, ?, ?, ?)");
            pstmt.setInt(1, athlete.getId());
            pstmt.setString(2, athlete.getName());
            pstmt.setString(3, athlete.getCountry());
            pstmt.setBoolean(4, athlete.getIsActive());
            pstmt.execute();
            return OK;
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Athlete getAthleteProfile(Integer athleteId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        Athlete athlete;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM Athletes WHERE Athlete_Id = ?");
            pstmt.setInt(1, athleteId);
            ResultSet result = pstmt.executeQuery();
            athlete = resultToAthlete(result);
            result.close();
        }
        catch (SQLException e) {
            return Athlete.badAthlete();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return Athlete.badAthlete();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return Athlete.badAthlete();
            }
        }
        return athlete;
    }

    public static ReturnValue deleteAthlete(Athlete athlete) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM Athletes WHERE Athlete_Id = ?");
            pstmt.setInt(1, athlete.getId());
            if (pstmt.executeUpdate() == 0) {
                return ReturnValue.NOT_EXISTS;
            }
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return OK;
    }

    public static ReturnValue addSport(Sport sport) {
        PreparedStatement pstmt = null;
        Connection connection = DBConnector.getConnection();
        if (sport == null) return ReturnValue.BAD_PARAMS;
        try {
            pstmt = connection.prepareStatement("INSERT INTO Sports" + " VALUES (?, ?, ?)");
            pstmt.setInt(1, sport.getId());
            pstmt.setString(2, sport.getName());
            pstmt.setString(3, sport.getCity());
            pstmt.execute();
            return OK;
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Sport getSport(Integer sportId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        Sport sport;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM Sports WHERE Sport_Id = ?");
            pstmt.setInt(1, sportId);
            ResultSet result = pstmt.executeQuery();
            sport = resultToSport(result);
            result.close();
        }
        catch (SQLException e) {
            return Sport.badSport();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return Sport.badSport();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return Sport.badSport();
            }
        }
        return sport;
    }

    public static ReturnValue deleteSport(Sport sport) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM Sports WHERE Sport_Id = ?");
            pstmt.setInt(1,sport.getId());
            if (pstmt.executeUpdate() == 0) {
                return ReturnValue.NOT_EXISTS;
            }
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return OK;
    }

    public static ReturnValue athleteJoinSport(Integer sportId, Integer athleteId) {
        ReturnValue result = OK;
        Athlete a = getAthleteProfile(athleteId);
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try{
            pstmt = connection.prepareStatement("SELECT * FROM Athletes WHERE Athlete_Id=?");
            pstmt.setInt(1, athleteId);
            ResultSet pre_result = pstmt.executeQuery();
            if (pre_result.next() == false) {
                pre_result.close();
                result = NOT_EXISTS;
            }
            else {
                pstmt = connection.prepareStatement("SELECT * FROM Sports WHERE Sport_Id=?");
                pstmt.setInt(1, sportId);
                ResultSet sport_pre_result = pstmt.executeQuery();
                if (sport_pre_result.next() == false) {
                    sport_pre_result.close();
                    result = NOT_EXISTS;
                }
                else {
                    pstmt = connection.prepareStatement("SELECT * FROM Involved WHERE Athlete_Id=? and Sport_Id=?");
                    pstmt.setInt(1, athleteId);
                    pstmt.setInt(2, sportId);
                    ResultSet involved_pre_result = pstmt.executeQuery();
                    if (involved_pre_result.next()) {
                        involved_pre_result.close();
                        result = ALREADY_EXISTS;
                    }
                    else {
                        pstmt = connection.prepareStatement("INSERT INTO Involved" + " VALUES (?, ?, ?)");
                        pstmt.setInt(1, sportId);
                        pstmt.setInt(2, athleteId);
                        boolean is_active = a.getIsActive();
                        if (is_active) pstmt.setInt(3, 0);
                        else pstmt.setInt(3, 100);
                        pstmt.execute();
                        pstmt = connection.prepareStatement("UPDATE Sports SET athletes_counter = athletes_counter + 1 WHERE Sport_Id = ?");
                        pstmt.setInt(1, sportId);
                        if (is_active) pstmt.execute();
                    }
                }
            }
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return result;
    }

    public static ReturnValue athleteLeftSport(Integer sportId, Integer athleteId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        Athlete athlete = getAthleteProfile(athleteId);
        try {
            pstmt = connection.prepareStatement("DELETE FROM Involved WHERE Sport_Id = ? AND Athlete_Id = ?");
            pstmt.setInt(1,sportId);
            pstmt.setInt(2,athleteId);
            if (pstmt.executeUpdate() == 0) {
                return ReturnValue.NOT_EXISTS;
            }
            pstmt = connection.prepareStatement("UPDATE Sports SET Athletes_Counter = Athletes_Counter - 1 WHERE Sport_Id = ?");
            pstmt.setInt(1,sportId);
            if(athlete.getIsActive() == true)  pstmt.execute();
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return OK;
    }

    public static ReturnValue confirmStanding(Integer sportId, Integer athleteId, Integer place) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ReturnValue res = OK;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM Athletes WHERE Athlete_id=? AND Active=?");
            pstmt.setInt(1, athleteId);
            pstmt.setBoolean(2, true);
            ResultSet result = pstmt.executeQuery();
            if (result.next() == false) {
                res = NOT_EXISTS;
            }
            else {
                pstmt = connection.prepareStatement("UPDATE Involved SET ranking = ? WHERE sport_Id = ? AND athlete_Id = ?");
                pstmt.setInt(1,place);
                pstmt.setInt(2,sportId);
                pstmt.setInt(3,athleteId);
                if (pstmt.executeUpdate() == 0) {
                    res = NOT_EXISTS;
                }
            }
        }
        catch (SQLException e){
            return convert(e);
        }
        finally {
            try{
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return res;
    }

    public static ReturnValue athleteDisqualified(Integer sportId, Integer athleteId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("UPDATE Involved SET ranking = null WHERE sport_Id = ? AND athlete_Id = ?");
            pstmt.setInt(1,sportId);
            pstmt.setInt(2,athleteId);
            if (pstmt.executeUpdate() == 0) return NOT_EXISTS;
        }
        catch (SQLException e){
            return convert(e);
        }
        finally {
            try{
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return OK;
    }

    public static ReturnValue makeFriends(Integer athleteId1, Integer athleteId2) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ReturnValue res = OK;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM Friendships WHERE Athlete_Id1 = ? AND Athlete_Id2 = ?");
            pstmt.setInt(1, athleteId1);
            pstmt.setInt(2, athleteId2);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                res = ALREADY_EXISTS;
            }
            else {
                pstmt = connection.prepareStatement("INSERT INTO Friendships" +
                        " VALUES (?, ?)");
                pstmt.setInt(1, athleteId1);
                pstmt.setInt(2, athleteId2);
                pstmt.execute();
                pstmt = connection.prepareStatement("INSERT INTO Friendships" +
                        " VALUES (?, ?)");
                pstmt.setInt(1, athleteId2);
                pstmt.setInt(2, athleteId1);
                pstmt.execute();
            }
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return res;
    }

    public static ReturnValue removeFriendship(Integer athleteId1, Integer athleteId2) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM Friendships" +
                    " WHERE Athlete_Id1 = ? AND Athlete_Id2 = ?");
            pstmt.setInt(1, athleteId1);
            pstmt.setInt(2, athleteId2);
            if (pstmt.executeUpdate() == 0) {
                return ReturnValue.NOT_EXISTS;
            }
            pstmt = connection.prepareStatement("DELETE FROM Friendships" +
                    " WHERE Athlete_Id1 = ? AND Athlete_Id2 = ?");
            pstmt.setInt(1, athleteId2);
            pstmt.setInt(2, athleteId1);
            if (pstmt.executeUpdate() == 0) {
                return ReturnValue.NOT_EXISTS;
            }
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return OK;
    }

    public static ReturnValue changePayment(Integer athleteId, Integer sportId, Integer payment) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ReturnValue res = OK;
        try {
            pstmt = connection.prepareStatement("UPDATE Involved\n" +
                    "SET Payment = ?\n" +
                    "WHERE Athlete_Id = ? AND Sport_Id = ?\n" +
                    "AND EXISTS(SELECT * FROM Athletes WHERE Active=false AND Athlete_Id = ?)");
            pstmt.setInt(1, payment);
            pstmt.setInt(2, athleteId);
            pstmt.setInt(3, sportId);
            pstmt.setInt(4, athleteId);
            if (pstmt.executeUpdate() == 0) {
                res = NOT_EXISTS;
            }
        }
        catch (SQLException e) {
            return convert(e);
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return ReturnValue.ERROR;
            }
        }
        return res;
    }

    public static Boolean isAthletePopular(Integer athleteId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            Athlete myAthlete = getAthleteProfile(athleteId);
            if (myAthlete.getId() != -1) {
                pstmt = connection.prepareStatement("select Sport_id from Involved where Athlete_Id in\n" +
                        " (SELECT Athlete_Id2 FROM Friendships WHERE Athlete_Id1=?)\n " +
                        "and Sport_id not in (SELECT Sport_id from Involved WHERE Athlete_Id=?)\n");
                pstmt.setInt(1, myAthlete.getId());
                pstmt.setInt(2, myAthlete.getId());
                ResultSet result = pstmt.executeQuery();
                if (result.next() == false) {
                    return true;
                }
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    return false;
                }
            }
        }
        catch (SQLException e) {
            return false;
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    public static Integer getTotalNumberOfMedalsFromCountry(String country) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT COUNT(ranking) AS numOfMedals FROM v1  WHERE country = ?");
            pstmt.setString(1, country);
            ResultSet result = pstmt.executeQuery();
            result.next();
            return result.getInt("numOfMedals");
        }
        catch (SQLException e) {
            return 0;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return 0;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return 0;
            }
        }
    }

    public static Integer getIncomeFromSport(Integer sportId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT SUM(Payment) AS totalIncome FROM Involved WHERE Sport_Id = ?");
            pstmt.setInt(1, sportId);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) return result.getInt("totalIncome");
        }
        catch (SQLException e) {
            return 0;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return 0;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return 0;
            }
        }
        return 0;
    }

    public static String getBestCountry() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT count(GetMedalsAthlete.athlete_id) as medals, country FROM GetMedalsAthlete\n" +
                    "join athletes on GetMedalsAthlete.athlete_id = athletes.athlete_id\n" +
                    "group by country\n" +
                    "order by medals desc, country asc");
            ResultSet results = pstmt.executeQuery();
            if (results.next()) return results.getString("country");
            else return "";
        }
        catch (SQLException e) {
            return null;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return null;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return null;
            }
        }
    }

    public static String getMostPopularCity() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("select AthletesPercity.city as bestCity, (1.0*numAthletes)/numSports as avreagepercity from AthletesPercity join sportspercity\n" +
                    "on AthletesPercity.city = sportspercity.city\n" +
                    "order by avreagepercity desc , AthletesPercity.city desc");
            ResultSet results = pstmt.executeQuery();
            if (results.next()) return results.getString("bestCity");
            else return "";
        }

        catch (SQLException e) {
            return null;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return null;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return null;
            }
        }
    }

    public static ArrayList<Integer> getAthleteMedals(Integer athleteId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        ArrayList<Integer> badArrayList = new ArrayList<Integer>();
        int i=1;
        while (i<=3) {
            badArrayList.add(0);
            i++;
        }
        try {
            int j=1;
            while (j<=3) {
                pstmt = connection.prepareStatement("SELECT count(Sport_Id) AS Medals " +
                        "FROM Athletes JOIN Involved ON Athletes.Athlete_Id = Involved.Athlete_Id " +
                        "WHERE Athletes.Athlete_Id=? and Ranking=?");
                pstmt.setInt(1, athleteId);
                pstmt.setInt(2, j);
                ResultSet result = pstmt.executeQuery();
                if (result.next()) arrayList.add(result.getInt("Medals"));
                else arrayList.add(0);
                result.close();
                j++;
            }
        }
        catch (SQLException e) {
            return badArrayList;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return badArrayList;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return badArrayList;
            }
        }
        return arrayList;
    }

    public static ArrayList<Integer> getMostRatedAthletes() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> topRated = new ArrayList<Integer>();
        try {
            pstmt = connection.prepareStatement("SELECT Athlete_Id, sum(Points) AS TotalPoints\n" +
                    "FROM GetPoints " +
                    "GROUP BY Athlete_Id\n" +
                    "ORDER BY TotalPoints DESC, Athlete_Id ASC\n" +
                    "LIMIT 10");
            ResultSet result = pstmt.executeQuery();
            int i=1;
            while (result.next() && i<=10) {
                topRated.add(result.getInt("Athlete_Id"));
                i++;
            }
            result.close();
        }
        catch (SQLException e) {
            return new ArrayList<Integer>();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                return new ArrayList<Integer>();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                return new ArrayList<Integer>();
            }
        }
        return topRated;
    }

    public static ArrayList<Integer> getCloseAthletes(Integer athleteId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> CloseFriends = new ArrayList<Integer>();
        try {
            Athlete athlete = getAthleteProfile(athleteId);
            if (athlete.getId() != -1) {
                pstmt = connection.prepareStatement("select count(sport_id) as counter from involved where athlete_id=?");
                pstmt.setInt(1, athleteId);
                ResultSet res = pstmt.executeQuery();
                if (res.next()) {
                    if (res.getInt("counter") == 0) {
                        pstmt = connection.prepareStatement("select * from athletes\n" +
                                "where athlete_id != ?\n" +
                                "order by athlete_id asc");
                        pstmt.setInt(1, athleteId);
                    }
                    else {
                        pstmt = connection.prepareStatement("select athlete_id, count(sport_id) as SportCount\n" +
                                "from involved \n" +
                                "where sport_id in (select sport_id from involved where athlete_id=?) and athlete_id != ?\n" +
                                "group by athlete_id\n" +
                                "having count(sport_id) * 2 >= (select count(sport_id) from involved where athlete_id=?)\n" +
                                "order by athlete_id asc");
                        pstmt.setInt(1,athleteId);
                        pstmt.setInt(2, athleteId);
                        pstmt.setInt(3,  athleteId);
                    }
                }
                ResultSet res2 = pstmt.executeQuery();
                int i=1;
                while (res2.next() && i<=10) {
                    CloseFriends.add(res2.getInt("athlete_id"));
                    i++;
                }
                res2.close();
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    return new ArrayList<Integer>();
                }
            }
        }
        catch (SQLException e) {
            return new ArrayList<Integer>();
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                return new ArrayList<Integer>();
            }
        }
        return CloseFriends;
    }

    public static ArrayList<Integer> getSportsRecommendation(Integer athleteId) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> CloseFriendsSports = new ArrayList<Integer>();
        try {
            Athlete athlete = getAthleteProfile(athleteId);
            if (athlete.getId() != -1) {
                pstmt = connection.prepareStatement("select count(sport_id) as counter from involved where athlete_id=?");
                pstmt.setInt(1, athleteId);
                ResultSet res = pstmt.executeQuery();
                if (res.next()) {
                    if (res.getInt("counter") == 0) {
                        pstmt = connection.prepareStatement("select sport_id, count(athlete_id) as counter\n" +
                                "from involved\n" +
                                "group by sport_id\n" +
                                "order by counter desc, sport_id asc\n" +
                                "limit 3");
                    }
                    else {
                        pstmt = connection.prepareStatement("select sport_id, count(involved.athlete_id) as numAthletes\n" +
                                "from (select athlete_id, count(sport_id) as SportCount\n" +
                                "from involved \n" +
                                "where sport_id in (select sport_id from involved where athlete_id=?) and athlete_id != ?\n" +
                                "group by athlete_id\n" +
                                "having count(sport_id) * 2 >= (select count(sport_id) from involved where athlete_id=?)\n" +
                                "order by athlete_id asc) as CloseAthletes\n" +
                                "join involved on involved.athlete_id = CloseAthletes.athlete_id\n" +
                                "where sport_id not in (select sport_id from involved where athlete_id=?)\n" +
                                "group by sport_id\n" +
                                "order by numAthletes desc, sport_id asc");
                        pstmt.setInt(1, athleteId);
                        pstmt.setInt(2, athleteId);
                        pstmt.setInt(3, athleteId);
                        pstmt.setInt(4, athleteId);
                    }
                }
                pstmt.executeQuery();
                ResultSet res2 = pstmt.executeQuery();
                int i=0;
                while (res2.next() && i<3) {
                    CloseFriendsSports.add(res2.getInt("Sport_id"));
                    i++;
                }
                res2.close();
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    return new ArrayList<Integer>();
                }
            }
        }
        catch (SQLException e) {
            return new ArrayList<Integer>();
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                return new ArrayList<Integer>();
            }
        }
        return CloseFriendsSports;
    }
}

