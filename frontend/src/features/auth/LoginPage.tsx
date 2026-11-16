import { apiRequest } from "../../lib/api-client"
import { setSession, type Session } from "../../lib/session"
import { queryClient } from "../../lib/query-client"
import { useState } from "react"
import { useNavigate } from "react-router"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Boxes, Eye, EyeOff, ShieldCheck } from "lucide-react"
import { Button, Input } from "../../components/ui"

const schema = z.object({
  email: z.email("Informe um e-mail válido."),
  password: z.string().min(1, "Informe sua senha."),
})
type LoginData = z.infer<typeof schema>

export function LoginPage() {
  const [visible, setVisible] = useState(false)
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginData>({ resolver: zodResolver(schema) })
  const [error, setError] = useState("")
  const submit = async (data: LoginData) => {
    setError("")
    try {
      const result = await apiRequest<Omit<Session, "expiresAt">>(
        "/auth/login",
        {
          method: "POST",
          body: JSON.stringify({
            email: data.email.trim(),
            senha: data.password,
          }),
        },
      )
      queryClient.clear()
      setSession({ ...result, expiresAt: Date.now() + result.expiresIn * 1000 })
      navigate("/dashboard", { replace: true })
    } catch (error) {
      setError((error as Error).message)
    }
  }
  return (
    <main className="login-page">
      <section className="login-aside">
        <div className="login-graphic">
          <div>
            <Boxes />
          </div>
          <span />
          <span />
          <span />
        </div>
        <div>
          <h1>
            Controle seu estoque.
            <br />
            <em>Decida com clareza.</em>
          </h1>
          <p>
            Dados operacionais e financeiros reunidos em um só lugar para uma
            gestão mais inteligente.
          </p>
        </div>
        <div className="security-note">
          <ShieldCheck size={18} />
          <span>Acesso seguro à sua empresa.</span>
        </div>
      </section>
      <section className="login-form-wrap">
        <form className="login-form" onSubmit={handleSubmit(submit)}>
          <div className="brand">
            <div className="brand-mark">
              <Boxes />
            </div>
            <strong>
              Stock<span>IA</span>
            </strong>
          </div>
          <div>
            <h2>Boas-vindas</h2>
            <p>Acesse sua conta para continuar.</p>
          </div>
          {error && (
            <p className="field-error" role="alert">
              {error}
            </p>
          )}
          <label>
            E-mail
            <Input
              type="email"
              autoComplete="username"
              {...register("email")}
              placeholder="voce@empresa.com"
            />
            {errors.email && (
              <span className="field-error">{errors.email.message}</span>
            )}
          </label>
          <label>
            Senha
            <div className="password-field">
              <Input
                type={visible ? "text" : "password"}
                autoComplete="current-password"
                {...register("password")}
                placeholder="Sua senha"
              />
              <button
                type="button"
                onClick={() => setVisible(!visible)}
                aria-label="Mostrar ou ocultar senha"
              >
                {visible ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {errors.password && (
              <span className="field-error">{errors.password.message}</span>
            )}
          </label>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Entrando..." : "Entrar na plataforma"}
          </Button>
        </form>
      </section>
    </main>
  )
}
