package vn.kltn.controller.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.service.IRepoService;

import java.util.Date;

@Controller
@RequestMapping("/repository")
@RequiredArgsConstructor
public class RepositoryController {
    private final IRepoService repoService;
    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/invitation/accept")
    public String acceptInvitation(@RequestParam("repoId") Long repositoryId, @RequestParam String token, Model model) {
        RepoResponseDto repoResponseDto = repoService.acceptInvitation(repositoryId, token);
        model.addAttribute("repo", repoResponseDto);
//        model.addAttribute("baseUrl", baseUrl);
        return "invitation-accept-success";
    }

    @GetMapping("/invitation/reject")
    public String rejectInvitation(@RequestParam("repoId") Long repositoryId, @RequestParam String email, Model model) {
        RepoResponseDto repoResponseDto = repoService.rejectInvitation(repositoryId, email);
        model.addAttribute("repo", repoResponseDto);
//        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("timeOfReject", new Date());
        return "invitation-rejected";
    }
}
