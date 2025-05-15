package com.api.rest_api.dto;

import com.api.rest_api.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {
    private long id;
    private String username;
    private String pfp;
    private long coins;

    public UserModel(Account account) {
        this.id = account.getUid();
        this.username = account.getUsername();
        this.pfp = account.getImage();
        this.coins = account.getCoins();
    }
}
