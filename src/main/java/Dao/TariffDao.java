package Dao;

import Models.Prices;
import utils.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TariffDao implements Dao<Prices, Long>{
    private final static String FIND_ALL = "SELECT * FROM prices";
    private static final String DELETE_BY_ID = "DELETE FROM prices WHERE price_id=?";
    private final static String FIND_BY_ROOM_ID = "SELECT * FROM prices WHERE type_id = ?";
    private final static String SAVE = "INSERT INTO prices(tariff, price,type_id) " +
            "VALUES (?,?::numeric, ?)";

    @Override
    public Prices findById(Long aLong) {
        return null;
    }

    @Override
    public Collection<Prices>  findByRoomId(Long typeId) throws Exception {
        List<Prices> list ;
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_BY_ROOM_ID);
        statement.setLong(1, typeId);
        try (ResultSet rs = statement.executeQuery()) {
            list = mapper(rs);
        }

        return  list;
    }

    @Override
    public Collection<Prices> findAll() throws Exception {
        List<Prices> list;
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_ALL);
        ResultSet rs = statement.executeQuery();

        list = mapper(rs);

        return list;
    }

    private List<Prices> mapper(ResultSet rs) {
        List<Prices> list = new ArrayList<>();
        try {
            while (rs.next()) {
                list.add(new Prices(rs.getInt("price_id"),
                        rs.getString("tariff"),
                        rs.getDouble("price"),
                        rs.getInt("type_id")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    @Override
    public Prices save(Prices prices) throws Exception {
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SAVE, new String[]{"price_id"});
        statement.setString(1, prices.getTariff());
        statement.setDouble(2, prices.getPrice() );
        statement.setLong(3, prices.getTypeId());

        statement.executeUpdate();

        try (ResultSet rs = statement.getGeneratedKeys()) {
            if (rs.next()) {
                prices.setId(rs.getLong("price_id"));
            }
        }

        return prices;
    }

    @Override
    public Prices update(Prices entity) {
        return null;
    }


    @Override
    public void deleteById(long id) throws Exception {
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID);
        statement.setLong(1, id);
        statement.executeUpdate();

    }

    @Override
    public List<Prices> search(String query) {
        return List.of();
    }
}
