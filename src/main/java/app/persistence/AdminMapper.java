package app.persistence;

//import app.entities.User;
import app.entities.Order;
import app.entities.Orderline;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminMapper {

    public static ArrayList<User> showCustomer(String userName,ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<User> customerInfo = new ArrayList<>();
        String sql = "select * from public.users where name=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, userName);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int user_ID = rs.getInt("user_id");
                String email = rs.getString("email");
                String user_Name = rs.getString("name");
                String user_Mobile = rs.getString("mobile");
                int user_Balance = rs.getInt("balance");
                User newUser = new User(user_ID,email,"",false,user_Name,user_Mobile,user_Balance);
                customerInfo.add(newUser);
            } else {
                //throw new DatabaseException("Fejl i login. Prøv igen");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
        return customerInfo;
    }

    public static ArrayList<Order> showCustomersOrders(String username, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> orderList = new ArrayList<>();
        int userId = UserMapper.getUserId(username, connectionPool);
        String sql = "select * from orders WHERE user_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int order_id = rs.getInt("order_id");
                String orderDate = rs.getString("orderdate");
                int orderPrice = rs.getInt("orderprice");
                boolean status = rs.getBoolean("status");
                orderList.add(new Order(order_id, userId, orderDate, orderPrice, status));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl!!!!", e.getMessage());
        }
        return orderList;
    }

    public static ArrayList<Orderline> showCustomerOrders(String userName, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Orderline> orderList = new ArrayList<>();
        String query = "SELECT u.user_id, u.email, u.name, u.mobile, u.balance, t.topping, b.bottom, ol.quantity, ol.price AS orderline_price " +
                "FROM public.orders o " +
                "JOIN public.users u ON o.user_id = u.user_id " +
                "JOIN public.orderline ol ON o.order_id = ol.order_id " +
                "JOIN public.topping t ON ol.topping_id = t.topping_id " +
                "JOIN public.bottom b ON ol.bottom_id = b.bottom_id " +
                "WHERE u.name = ?;";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1,userName);
            ResultSet resultSet = ps.executeQuery();
            // Process the result set
            while (resultSet.next()) {
                // Retrieve data from each row
                int userId = resultSet.getInt("user_id");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                String mobile = resultSet.getString("mobile");
                int balance = resultSet.getInt("balance");
                String topping = resultSet.getString("topping");
                String bottom = resultSet.getString("bottom");
                int quantity = resultSet.getInt("quantity");
                int orderlinePrice = resultSet.getInt("orderline_price");

                Orderline newOrder = new Orderline(userId,email, name, mobile, balance, topping, bottom, quantity, orderlinePrice);

                orderList.add(newOrder);
            }
            return orderList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Orderline> showAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Orderline> orderList = new ArrayList<>();
        String query = "SELECT u.user_id,ol.order_id, u.email, u.name, u.mobile, u.balance, t.topping, b.bottom, ol.quantity, ol.price AS orderline_price " +
                "FROM public.orders o " +
                "JOIN public.users u ON o.user_id = u.user_id " +
                "JOIN public.orderline ol ON o.order_id = ol.order_id " +
                "JOIN public.topping t ON ol.topping_id = t.topping_id " +
                "JOIN public.bottom b ON ol.bottom_id = b.bottom_id";
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ResultSet resultSet = ps.executeQuery();
            // Process the result set
            while (resultSet.next()) {
                // Retrieve data from each row
                int userId = resultSet.getInt("user_id");
                int orderId = resultSet.getInt("order_id");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                String mobile = resultSet.getString("mobile");
                int balance = resultSet.getInt("balance");
                String topping = resultSet.getString("topping");
                String bottom = resultSet.getString("bottom");
                int quantity = resultSet.getInt("quantity");
                int orderlinePrice = resultSet.getInt("orderline_price");

                Orderline newOrder = new Orderline(userId,/*orderId,*/ email, name, mobile, balance, topping, bottom, quantity, orderlinePrice);

                orderList.add(newOrder);
            }
            return orderList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean addMoneyToCustomerBalance(String email, String amountToAdd, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "UPDATE public.users " +
                "SET balance = balance + ? "+
                "WHERE email = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, Integer.parseInt(amountToAdd));
            ps.setString(2, email);

            int rowsUpdated = ps.executeUpdate(); // Execute the update operation
            return rowsUpdated > 0;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }
}