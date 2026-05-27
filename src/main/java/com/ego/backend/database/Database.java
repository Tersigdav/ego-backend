package com.ego.backend.database;

import com.ego.backend.entity.Room;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class Database implements DatabaseInterface {
    private Connection database;

    public Database() throws SQLException, ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
        this.database = DriverManager.getConnection("jdbc:mariadb://localhost:3306/Ego", "root", "password");
    }

    public void chiudiConnessione() {
        try {
            if (!this.database.isClosed())
                this.database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Room> getRooms(
            String nome,
            String descrizione,
            Boolean esplorabile,
            String imgUrl
    ) throws SQLException {
        List<Room> rooms = new ArrayList<>();

        StringBuilder  queryBuilder = new StringBuilder("SELECT * FROM Rooms WHERE 1=1");

        if (nome != null) queryBuilder.append(" AND name LIKE ?");
        if (descrizione != null) queryBuilder.append(" AND description LIKE ?");
        if (esplorabile != null) queryBuilder.append(" AND explorable = ?");
        if (imgUrl != null) queryBuilder.append(" AND img_url LIKE ?");

        try (PreparedStatement ps = this.database.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            if (nome != null) ps.setString(paramIndex++, "%" + nome + "%");
            if (descrizione != null) ps.setString(paramIndex++, "%" + descrizione + "%");
            if (esplorabile != null) ps.setBoolean(paramIndex++, esplorabile);
            if (imgUrl != null) ps.setString(paramIndex++, "%" + imgUrl + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room();
                    room.setId(rs.getLong("id"));
                    room.setName(rs.getString("name"));
                    room.setDescription(rs.getString("description"));
                    room.setExplorable(rs.getBoolean("explorable"));
                    room.setImgUrl(rs.getString("img_url"));
                    rooms.add(room);
                }
            }
            return rooms;
        }
    }
    public Map<String, Long> getExitsForRoom(Long roomId) throws SQLException {
        Map<String, Long> exits = new HashMap<>();

        String query = "SELECT direction, target_room_id FROM room_exits WHERE room_id = ?";
        try (PreparedStatement ps = this.database.prepareStatement(query)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    exits.put(rs.getString("direction"), rs.getLong("target_room_id"));
                }
            }
        }
        return exits;
    }
}
