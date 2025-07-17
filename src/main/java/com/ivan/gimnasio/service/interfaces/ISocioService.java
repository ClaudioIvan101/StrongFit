package com.ivan.gimnasio.service.interfaces;

import com.ivan.gimnasio.persistence.entity.Socio;
import java.util.List;
public interface ISocioService {
    Socio registrarSocio(Socio socio);
    List<Socio> listarSocios();

}
