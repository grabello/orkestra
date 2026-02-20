variable "env_prefix" {
  type        = string
  description = "Environment prefix used in resource names (local/dev/prod)."
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}
