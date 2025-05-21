package com.group06.music_app_mobile.api_client.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAuthRequest {
    private String tokenId;

//    public GoogleAuthRequest(String tokenId){
//        this.tokenId = tokenId;
//    }
}
