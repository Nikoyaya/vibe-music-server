package org.amis.vibemusicserver.service;

import org.amis.vibemusicserver.model.dto.TokenDTO;
import org.amis.vibemusicserver.model.dto.TokenRefreshDTO;
import org.amis.vibemusicserver.result.Result;

public interface TokenService {
    Result<TokenDTO> generateRefreshToken(TokenRefreshDTO refreshDTO);
}
