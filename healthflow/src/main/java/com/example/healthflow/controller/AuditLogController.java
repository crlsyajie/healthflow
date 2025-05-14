package com.example.healthflow.controller;

import com.example.healthflow.model.AuditLog;
import com.example.healthflow.model.User;
import com.example.healthflow.repository.UserRepository;
import com.example.healthflow.service.AuditLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String viewAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String username,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check if any filters are applied
        boolean hasAction = action != null && !action.isEmpty();
        boolean hasEntityType = entityType != null && !entityType.isEmpty();
        boolean hasDateRange = startDate != null && endDate != null;
        boolean hasUsername = username != null && !username.trim().isEmpty();
        
        boolean hasFilters = hasAction || hasEntityType || hasDateRange || hasUsername;
        
        if (hasFilters) {
            try {
                List<AuditLog> logs = new ArrayList<>();
                
                // Apply filters based on priority
                if (hasAction) {
                    System.out.println("Filtering by action: " + action);
                    logs = auditLogService.findByAction(action);
                    System.out.println("Found " + logs.size() + " logs with action: " + action);
                    
                    // Apply additional filters
                    if (hasEntityType) {
                        System.out.println("Additionally filtering by entity type: " + entityType);
                        List<AuditLog> filtered = new ArrayList<>();
                        for (AuditLog log : logs) {
                            if (log.getEntityType().equals(entityType)) {
                                filtered.add(log);
                            }
                        }
                        logs = filtered;
                        System.out.println("Remaining logs after entity type filter: " + logs.size());
                    }
                    
                    if (hasDateRange) {
                        System.out.println("Additionally filtering by date range: " + startDate + " to " + endDate);
                        List<AuditLog> filtered = new ArrayList<>();
                        for (AuditLog log : logs) {
                            LocalDateTime createdAt = log.getCreatedAt();
                            if (createdAt.isEqual(startDate) || createdAt.isEqual(endDate) || 
                                (createdAt.isAfter(startDate) && createdAt.isBefore(endDate))) {
                                filtered.add(log);
                            }
                        }
                        logs = filtered;
                        System.out.println("Remaining logs after date range filter: " + logs.size());
                    }
                    
                    if (hasUsername) {
                        System.out.println("Additionally filtering by username: " + username);
                        User user = userRepository.findByUsername(username.trim());
                        if (user != null) {
                            List<AuditLog> filtered = new ArrayList<>();
                            for (AuditLog log : logs) {
                                if (log.getUser() != null && log.getUser().getId().equals(user.getId())) {
                                    filtered.add(log);
                                }
                            }
                            logs = filtered;
                            System.out.println("Remaining logs after username filter: " + logs.size());
                        } else {
                            System.out.println("No user found with username: " + username);
                            logs.clear();
                        }
                    }
                } else if (hasEntityType) {
                    System.out.println("Filtering by entity type: " + entityType);
                    logs = auditLogService.findByEntityTypeAndId(entityType, null);
                    System.out.println("Found " + logs.size() + " logs with entity type: " + entityType);
                    
                    // Apply additional filters
                    if (hasDateRange) {
                        System.out.println("Additionally filtering by date range");
                        List<AuditLog> filtered = new ArrayList<>();
                        for (AuditLog log : logs) {
                            LocalDateTime createdAt = log.getCreatedAt();
                            if (createdAt.isEqual(startDate) || createdAt.isEqual(endDate) || 
                                (createdAt.isAfter(startDate) && createdAt.isBefore(endDate))) {
                                filtered.add(log);
                            }
                        }
                        logs = filtered;
                        System.out.println("Remaining logs after date range filter: " + logs.size());
                    }
                    
                    if (hasUsername) {
                        System.out.println("Additionally filtering by username");
                        User user = userRepository.findByUsername(username.trim());
                        if (user != null) {
                            List<AuditLog> filtered = new ArrayList<>();
                            for (AuditLog log : logs) {
                                if (log.getUser() != null && log.getUser().getId().equals(user.getId())) {
                                    filtered.add(log);
                                }
                            }
                            logs = filtered;
                            System.out.println("Remaining logs after username filter: " + logs.size());
                        } else {
                            System.out.println("No user found with username: " + username);
                            logs.clear();
                        }
                    }
                } else if (hasDateRange) {
                    System.out.println("Filtering by date range: " + startDate + " to " + endDate);
                    logs = auditLogService.findByDateRange(startDate, endDate);
                    System.out.println("Found " + logs.size() + " logs in date range");
                    
                    // Apply additional filters
                    if (hasUsername) {
                        System.out.println("Additionally filtering by username");
                        User user = userRepository.findByUsername(username.trim());
                        if (user != null) {
                            List<AuditLog> filtered = new ArrayList<>();
                            for (AuditLog log : logs) {
                                if (log.getUser() != null && log.getUser().getId().equals(user.getId())) {
                                    filtered.add(log);
                                }
                            }
                            logs = filtered;
                            System.out.println("Remaining logs after username filter: " + logs.size());
                        } else {
                            System.out.println("No user found with username: " + username);
                            logs.clear();
                        }
                    }
                } else if (hasUsername) {
                    System.out.println("Filtering by username: " + username);
                    User user = userRepository.findByUsername(username.trim());
                    if (user != null) {
                        logs = auditLogService.findByUser(user);
                        System.out.println("Found " + logs.size() + " logs for user: " + username);
                    } else {
                        System.out.println("No user found with username: " + username);
                        logs = new ArrayList<>();
                    }
                }
                
                model.addAttribute("logs", logs);
                System.out.println("Setting 'logs' attribute with " + logs.size() + " records");
                
                // Create a Page object with the filtered logs for consistency
                Page<AuditLog> auditLogsPage = new org.springframework.data.domain.PageImpl<>(logs, 
                    PageRequest.of(0, logs.size() > 0 ? logs.size() : 1), logs.size());
                model.addAttribute("auditLogsPage", auditLogsPage);
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", auditLogsPage.getTotalPages());
                System.out.println("Setting 'auditLogsPage' attribute for filtered logs");
                
                // Show filter summary in UI
                StringBuilder filterSummary = new StringBuilder();
                if (hasAction) {
                    filterSummary.append("Action: ").append(action);
                }
                if (hasEntityType) {
                    if (filterSummary.length() > 0) filterSummary.append(", ");
                    filterSummary.append("Entity Type: ").append(entityType);
                }
                if (hasDateRange) {
                    if (filterSummary.length() > 0) filterSummary.append(", ");
                    filterSummary.append("Date Range: ").append(startDate.toLocalDate()).append(" to ").append(endDate.toLocalDate());
                }
                if (hasUsername) {
                    if (filterSummary.length() > 0) filterSummary.append(", ");
                    filterSummary.append("Username: ").append(username);
                }
                
                if (filterSummary.length() > 0) {
                    model.addAttribute("filterSummary", filterSummary.toString());
                }
                
            } catch (Exception e) {
                System.err.println("Error applying filters: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "An error occurred while filtering logs: " + e.getMessage());
                return "redirect:/admin/audit-logs";
            }
        } else {
            // No filters, paginated view
            try {
                System.out.println("No filters applied, showing paginated view. Page: " + page + ", Size: " + size);
                Page<AuditLog> auditLogs = auditLogService.findAllPaginated(PageRequest.of(page, size));
                model.addAttribute("auditLogsPage", auditLogs);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", auditLogs.getTotalPages());
                System.out.println("Setting 'auditLogsPage' attribute with " + auditLogs.getNumberOfElements() + " records");
            } catch (Exception e) {
                System.err.println("Error fetching paginated logs: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "An error occurred while fetching logs: " + e.getMessage());
                return "redirect:/admin/audit-logs";
            }
        }

        return "admin/audit-logs";
    }

    @GetMapping("/export")
    public void exportToPdf(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String username,
            HttpServletResponse response) throws IOException {
        
        // Check if any filters are applied
        boolean hasAction = action != null && !action.isEmpty();
        boolean hasEntityType = entityType != null && !entityType.isEmpty();
        boolean hasDateRange = startDate != null && endDate != null;
        boolean hasUsername = username != null && !username.trim().isEmpty();
        
        boolean hasFilters = hasAction || hasEntityType || hasDateRange || hasUsername;
        
        List<AuditLog> logs = new ArrayList<>();
        
        if (hasFilters) {
            // Apply filters using same logic as view method
            if (hasAction) {
                logs = auditLogService.findByAction(action);
                
                // Apply additional filters
                if (hasEntityType) {
                    List<AuditLog> filtered = new ArrayList<>();
                    for (AuditLog log : logs) {
                        if (log.getEntityType().equals(entityType)) {
                            filtered.add(log);
                        }
                    }
                    logs = filtered;
                }
                
                if (hasDateRange) {
                    List<AuditLog> filtered = new ArrayList<>();
                    for (AuditLog log : logs) {
                        LocalDateTime createdAt = log.getCreatedAt();
                        if (createdAt.isEqual(startDate) || createdAt.isEqual(endDate) || 
                            (createdAt.isAfter(startDate) && createdAt.isBefore(endDate))) {
                            filtered.add(log);
                        }
                    }
                    logs = filtered;
                }
                
                if (hasUsername) {
                    User user = userRepository.findByUsername(username.trim());
                    if (user != null) {
                        List<AuditLog> filtered = new ArrayList<>();
                        for (AuditLog log : logs) {
                            if (log.getUser() != null && log.getUser().getId().equals(user.getId())) {
                                filtered.add(log);
                            }
                        }
                        logs = filtered;
                    } else {
                        logs.clear();
                    }
                }
            } else if (hasEntityType) {
                logs = auditLogService.findByEntityTypeAndId(entityType, null);
                
                // Apply additional filters
                if (hasDateRange) {
                    List<AuditLog> filtered = new ArrayList<>();
                    for (AuditLog log : logs) {
                        LocalDateTime createdAt = log.getCreatedAt();
                        if (createdAt.isEqual(startDate) || createdAt.isEqual(endDate) || 
                            (createdAt.isAfter(startDate) && createdAt.isBefore(endDate))) {
                            filtered.add(log);
                        }
                    }
                    logs = filtered;
                }
                
                if (hasUsername) {
                    User user = userRepository.findByUsername(username.trim());
                    if (user != null) {
                        List<AuditLog> filtered = new ArrayList<>();
                        for (AuditLog log : logs) {
                            if (log.getUser() != null && log.getUser().getId().equals(user.getId())) {
                                filtered.add(log);
                            }
                        }
                        logs = filtered;
                    } else {
                        logs.clear();
                    }
                }
            } else if (hasDateRange) {
                logs = auditLogService.findByDateRange(startDate, endDate);
                
                // Apply additional filters
                if (hasUsername) {
                    User user = userRepository.findByUsername(username.trim());
                    if (user != null) {
                        List<AuditLog> filtered = new ArrayList<>();
                        for (AuditLog log : logs) {
                            if (log.getUser() != null && log.getUser().getId().equals(user.getId())) {
                                filtered.add(log);
                            }
                        }
                        logs = filtered;
                    } else {
                        logs.clear();
                    }
                }
            } else if (hasUsername) {
                User user = userRepository.findByUsername(username.trim());
                if (user != null) {
                    logs = auditLogService.findByUser(user);
                }
            }
        } else {
            // No filters - get first 500 logs
            Page<AuditLog> auditLogs = auditLogService.findAllPaginated(PageRequest.of(0, 500));
            logs = auditLogs.getContent();
        }
        
        byte[] pdfBytes = auditLogService.exportLogsToPdf(logs);
        
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.pdf");
        response.setContentLength(pdfBytes.length);
        
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/{id}")
    public String viewLogDetail(@PathVariable Long id, Model model) {
        AuditLog log = auditLogService.findById(id);
        if (log == null) {
            return "redirect:/admin/audit-logs?error=Log+not+found";
        }
        
        model.addAttribute("log", log);
        return "admin/audit-log-detail";
    }
} 