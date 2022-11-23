package com.example.AuthService.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.AuthService.dto.LoginDTO;
import com.example.AuthService.dto.RegisterDTO;
import com.example.AuthService.dto.TokenDTO;
import com.example.AuthService.dto.UserPassDTO;
import com.example.AuthService.model.AuthControl;
import com.example.AuthService.model.ERole;
import com.example.AuthService.repository.AuthControlRepository;
import com.example.AuthService.security.TokenUtils;
import com.example.AuthService.service.AuthControlService;





@RestController
@RequestMapping("/auth")
public class UserController {


   @Autowired
   private AuthControlRepository authControlRepository;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;


    @Autowired
    TokenUtils tokenUtils;


    @PostMapping("/login")
    public ResponseEntity<TokenDTO> createAuthenticationToken(
            @RequestBody LoginDTO loginDTO, HttpServletResponse response) {

        // Ukoliko kredencijali nisu ispravni, logovanje nece biti uspesno, desice se
        // AuthenticationException
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(),loginDTO.getPassword()));

        // Ukoliko je autentifikacija uspesna, ubaci korisnika u trenutni security
        // kontekst
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Kreiraj token za tog korisnika
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(user);
        int expiresIn = tokenUtils.getExpiredIn();

        // Vrati token kao odgovor na uspesnu autentifikaciju
        System.out.println("xxxxxxxxxxxxxxxxxxx");       
        return ResponseEntity.ok(new TokenDTO(jwt));
    }
    
    @PostMapping(value = "/",consumes = "application/json")
    public ResponseEntity<UserPassDTO> register(
            @RequestBody RegisterDTO registerDTO, HttpServletResponse response) {
    	
    		AuthControl x = new AuthControl();
    		x.setUsername(registerDTO.getUsername());
    		x.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
    		x.setRole(ERole.valueOf(registerDTO.getRole()));
    		authControlRepository.save(x);
    		
    		UserPassDTO userPass = new UserPassDTO();
    		userPass.setAge(registerDTO.getAge());
    		userPass.setCity(registerDTO.getCity());
    		userPass.setName(registerDTO.getName());
    		userPass.setSex(registerDTO.getSex());
    		userPass.setSurname(registerDTO.getSurname());
    		userPass.setUsername(registerDTO.getUsername());
    		
    		RestTemplate restTemplate = new RestTemplate();
    		HttpEntity<UserPassDTO> request = new HttpEntity<>(userPass);
    		
    		String serviceUrl = "http://registracija:8082/users/";
    		 UserPassDTO UserPassResponse = restTemplate.postForObject(serviceUrl, 
    		  request, UserPassDTO.class);
		return new ResponseEntity<UserPassDTO>(userPass, HttpStatus.CREATED);
    		
       
    }


}
