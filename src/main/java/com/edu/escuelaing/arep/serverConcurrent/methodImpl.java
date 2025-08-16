package com.edu.escuelaing.arep.serverConcurrent;

import com.edu.escuelaing.arep.serverConcurrent.model.User;

import java.util.ArrayList;
import java.util.List;

public class methodImpl {
    private List<User> users = new ArrayList<>();

    public User getImplement(String userName){
        return users.stream()
                .filter(u -> u.getName().equals(userName))
                .findFirst()
                .orElse(null);
    }

    public String postImplement(User user){
        users.add(user);
        return "User added successfully";
    }

    public String deleteImplement(String name){
        boolean removed = users.removeIf(u -> u.getName().equals(name));
        if(removed){
            return "User deleted successfully";
        } else {
            return "User not found";
        }
    }

    public User putImplement(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getName().equals(user.getName())) {
                users.set(i, user);
                return user;
            }
        }
        users.add(user);
        return user;
    }

    public List<User> getAll(){
        return users;
    }


}
