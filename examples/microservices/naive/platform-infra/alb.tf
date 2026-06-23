# Public Application Load Balancer. Every service repo attaches its own
# target group + listener rule (on its path_prefix) to alb_listener_arn.
resource "aws_lb" "public" {
  name               = "${local.name}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = module.vpc.public_subnets

  idle_timeout               = 60
  enable_deletion_protection = false

  tags = merge(local.tags, { Name = "${local.name}-alb" })
}

# Default :80 listener. No service matches here by default, so the fixed 404
# is the catch-all. Service repos add higher-priority rules per path_prefix.
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.public.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "application/json"
      message_body = jsonencode({
        error   = "not_found"
        message = "No route matched. QuickBite services register their own listener rules."
      })
      status_code = "404"
    }
  }

  tags = local.tags
}
