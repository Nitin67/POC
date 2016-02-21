package premiumad.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.http.impl.client.CloseableHttpClient;

public class ConnectionUtils {

  public static void closeConnections(Connection connection) {
    if (null != connection) {
      try {
        connection.close();
      } catch (Exception e) {
        play.Logger.warn("Error in closing connection", e);
      }
    }
  }

  public static void closePreparedStatement(PreparedStatement preparedStatement) {
    if (null != preparedStatement) {
      try {
        preparedStatement.close();
      } catch (Exception e) {
        play.Logger.warn("Error in closing preparedStatement", e);
      }
    }
  }

  public static void closeResultSet(ResultSet rs) {
    if (null != rs) {
      try {
        rs.close();
      } catch (Exception e) {
        play.Logger.warn("Error in closing ResultSet", e);
      }
    }
  }

  public static void closeStreamReader(Reader reader){
    if (reader != null){
      
      try{
        reader.close();
      }
      catch(Exception e){
        
      }
    }
  }
  
  public static void closeOutputStreamWriter(OutputStreamWriter osw) {
    if (null != osw) {
      try {
        osw.flush();
      } catch (Exception e) {
      }
      try {
        osw.close();
      } catch (Exception e) {
      }
    }
  }

  public static void closeOutputStream(OutputStream osw) {
    if (null != osw) {
      try {
        osw.flush();
      } catch (Exception e) {
      }
      try {
        osw.close();
      } catch (Exception e) {
      }
    }
  }

  public static void closeInputStream(InputStream is) {
    if (null != is) {
      try {
        is.close();
      } catch (Exception e) {
        play.Logger.warn("Error in closing Input Stream", e);
      }
    }
  }

  public static void closeHttpClient(CloseableHttpClient client) {
    if (null != client) {
      try {
        client.close();
      } catch (Exception e) {
        play.Logger.warn("Error in closing Http Client connection", e);
      }
    }
  }
}
