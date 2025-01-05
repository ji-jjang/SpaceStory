package com.juny.spacestory.domain.space.user.controller;

import com.juny.spacestory.domain.space.common.dto.ReqCreateSpace;
import com.juny.spacestory.domain.space.common.entity.Space;
import com.juny.spacestory.domain.space.common.service.SpaceService;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpaceController {

  private SpaceService spaceService;

  @PostMapping("/v1/spaces")
  public ResponseEntity<Space> createSpace(@ModelAttribute ReqCreateSpace req) {

    return new ResponseEntity<>(spaceService.createSpace(req, -1L), HttpStatus.CREATED);
  }

  @GetMapping("/v1/spaces/{spaceId}")
  public ResponseEntity<Space> getSpaceDetail(@PathVariable Long spaceId) {

    return new ResponseEntity<>(spaceService.getSpaceDetailById(spaceId), HttpStatus.OK);
  }
}
