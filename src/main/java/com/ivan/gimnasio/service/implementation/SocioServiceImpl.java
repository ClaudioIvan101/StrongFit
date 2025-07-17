package com.ivan.gimnasio.service.implementation;

import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.persistence.repository.SocioRepository;
import com.ivan.gimnasio.service.interfaces.ISocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocioServiceImpl implements ISocioService {
    @Autowired
    private SocioRepository socioRepository;

    @Override
    public Socio registrarSocio(Socio socio) {
        return socioRepository.save(socio);
    }

    @Override
    public List<Socio> listarSocios() {
        return socioRepository.findAll();
    }
}

