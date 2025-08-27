package com.thalyson.digitalcourses.course_platform_backend.service;

import com.thalyson.digitalcourses.course_platform_backend.exception.EmailAlreadyInUseException;
import com.thalyson.digitalcourses.course_platform_backend.model.RoleJPA;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosAtualizacaoUser;
import com.thalyson.digitalcourses.course_platform_backend.model.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UserJPA registerNewUser(String email, String hashedPassword, String firstName, String lastName, Set<RoleJPA> roles){
        if (userRepository.findByEmail(email).isPresent()){
            throw new EmailAlreadyInUseException("Email já cadastrado!");
        }

        UserJPA newUser = new UserJPA(email, hashedPassword, firstName, lastName, roles);
        return userRepository.save(newUser);
    }

    public Optional<UserJPA> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public UserJPA update(Long id, DadosAtualizacaoUser dados) {
        UserJPA user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado com ID: " + id));

        user.updateFields(dados);
        return userRepository.save(user);
    }
}