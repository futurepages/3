package modules.admin.model.validators;

import edu.vt.middleware.dictionary.ArrayWordList;
import edu.vt.middleware.dictionary.WordListDictionary;
import edu.vt.middleware.dictionary.WordLists;
import edu.vt.middleware.dictionary.sort.ArraysSort;
import edu.vt.middleware.password.AlphabeticalCharacterRule;
import edu.vt.middleware.password.AlphabeticalSequenceRule;
import edu.vt.middleware.password.DictionarySubstringRule;
import edu.vt.middleware.password.Password;
import edu.vt.middleware.password.PasswordData;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.QwertySequenceRule;
import edu.vt.middleware.password.RepeatCharacterRegexRule;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.RuleResult;
import edu.vt.middleware.password.WhitespaceRule;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import modules.admin.model.entities.User;
import modules.admin.model.core.AdminConstants;
import modules.admin.model.dao.UserDao;
import org.futurepages.core.exception.DefaultExceptionLogger;
import org.futurepages.core.validation.Validator;
import org.futurepages.util.FileUtil;
import org.futurepages.util.Is;
import org.futurepages.util.StringUtils;

/**
 *
 * @author diogenes
 */
public class UserValidator extends Validator {

    public static final String SECUTIRY_ERROR = "A senha digitada não é segura. Evite sequências numéricas, caracteres repetidos, sequência de teclas vizinhas, espaços em branco e palavras conhecidas do dicionário.";

	public void validateCreate(User user) { 
		validateFullName(user);
		validateEmail(user);
		validateLogin(user);
		validatePasswordSecurity(user);
		
		validate();
	}
	
	public void validateUpdate(User user, User logado, String newPassword) {
		validateFullName(user);

		user = UserDao.get(user.getLogin());
		if (!logado.canUpdate(user)) {
			error("Seu perfil não permite alterar este usuário.");
		}
		if(newPassword!=null){
			user.setPassword(newPassword);
			validatePasswordSecurity(user);
		}
		validate();
	}

	public void validateFullName(User user) {
		if (Is.empty(user.getFullName())) {
			error("fullName", "Preencha o nome do usuário");
		}
	}

	public void validateEmail(User user) {
		if (Is.empty(user.getEmail())) {
			error("email", "Preencha o email do usuário");
		} else {
			User userWithEmail = UserDao.getByEmail(user.getEmail());
			if (userWithEmail != null && !userWithEmail.getLogin().equals(user.getLogin())) {
				error("email", "Este email já está cadastrado no sistema para outro usuário.");
			} else if (!Is.validMail(user.getEmail())) {
				error("email", "E-mail inválido");
			}
		}
	}

	public void validateLogin(User user) {
		if (Is.empty(user.getLogin())) {
			error("login", "Preencha o campo do login");
		} else if (!Is.validStringKey(user.getLogin())) {
			error("login", "O login digitado é inválido");
		} else if (UserDao.get(user.getLogin()) != null) {
			error("login", "Login digitado já existe");
		}
	}

//	public void confirmatedPasswords(User user, String confirmPassword) {
//		if (!user.getPlainPassword().equals(confirmPassword)) {
//			error("confirmPassword", "Senha de confirmação inválida");
//		}
//
//	}

	public void newPassword(User userDB, String password, String newPassword, String confirmNewPassword) {

		if (Is.empty(password) || Is.empty(newPassword) || Is.empty(confirmNewPassword)) {
			error("Preencha todos os campos do formulário");
		}

		if (!(userDB.getPassword().equals(userDB.encryptedPassword(password)))) {
			error("senhaAtual", "Senha Atual Inválida");
		}

		if (!newPassword.equals(confirmNewPassword)) {
			error("novaSenha", "Senha de confirmação não confere");
		}

		userDB.setPlainPassword(newPassword); //parece que é desnecessário este método aqui.
		validatePasswordSecurity(userDB);

//		validate(); //comentado pq na action deve estar breakOnFirst = true

	}

	public void newPasswordToForgottenPassword(User usuarioPersistente, String newPassword, String confirmNewPassword) {

		if (Is.empty(newPassword) || Is.empty(confirmNewPassword)) {
			error("Preencha todos os dados");
		}

		if (!newPassword.equals(confirmNewPassword)) {
			error("Senha de confirmação não confere");
		}

		usuarioPersistente.setPlainPassword(newPassword);
		validatePasswordSecurity(usuarioPersistente);

	}

	public void email(String login, String newEmail) {

		if (Is.empty(newEmail) || !Is.validMail(newEmail)) {
			error("Email inválido. Informe um email válido.");
		}

		if ((UserDao.getByEmail(newEmail) != null) && (!UserDao.isMailMine(login, newEmail))) {
			error("O email informado já está cadastrado para outro usuário. Informe outro email válido.");
		}
	}

	/*
	 * A senha deve ser diferente do login e de
	 * qualquer um dos tokens do nome completo
	 * e deve ser segura (várias regrinhas)
	 */
	public static String invalidPasswordMsg(User user) {
		String[] tokensNome;
		boolean hasError = false;
		String error = null;
		String errorSegurancaMinima = StringUtils.concat("Por questões de segurança, a senha deve possuir pelo menos ",
				String.valueOf(AdminConstants.MIN_SIZE_PASSWORD),
				" caracteres e não pode ser igual ao login e nem a um dos nomes do usuário.");

		if (Is.empty(user.getPlainPassword())) {
			error =  "Preencha o campo da senha";
			hasError = true;
		} else if (user.getPlainPassword().length() < AdminConstants.MIN_SIZE_PASSWORD) {
			error = errorSegurancaMinima;
			hasError = true;
		} else if (user.getPlainPassword().equalsIgnoreCase(user.getLogin())) {
			error =  errorSegurancaMinima;
			hasError = true;
		} else {
			tokensNome = user.getFullName().split(" ");
			for (String token : tokensNome) {
				if (user.getPlainPassword().equalsIgnoreCase(token)) {
					error = errorSegurancaMinima;
					hasError = true;
					break;
				}
			}
		}
		if(!hasError){
			if(!passwordIsSecure(user.getPlainPassword(), 4)){ //busca tokens com pelo menos len-4 caracteres.
				error =  SECUTIRY_ERROR;
			}
		}
		return error;
	}

    /*
     * Este método retorna true se a senha foi aceita ou false caso contrário.
     * Serão procuradas palavra no dicionário de tamanho no mínimo senha.length()-num
     * para saber se correspodem com alguma substring de senha, se correponder a senha
     * não será aceita, caso contrário ainda são avaliadas outras regras explicadas dentro do código.
     */
    public static boolean passwordIsSecure(String senha, int num) {
        AlphabeticalCharacterRule alphabeticalRule;
        WhitespaceRule whitespaceRule;
        QwertySequenceRule qwertySeqRule;
        AlphabeticalSequenceRule alphaSeqRule;
        RepeatCharacterRegexRule repeatCharRule;

        // Regra pra nao aceitar palavras contidas no dicionario de tamanho pelo menos o tamanho da senha menos 3
        dictRule.setWordLength(senha.length()-num);
        // Regra para a senha conter pelo menos um caractere alfabético
        alphabeticalRule = new AlphabeticalCharacterRule(1);
        // Regra pra não aceitar espaços em branco na senha
        whitespaceRule = new WhitespaceRule();
        // Regra pra não aceitar que a senha seja uma sequencia do teclado
        qwertySeqRule = new QwertySequenceRule(senha.length(), true);
        // Regra pra não aceitar que a senha que seja uma sequencia alfabética
        alphaSeqRule = new AlphabeticalSequenceRule(senha.length(), true);
        // Regra pra não aceitar que a senha seja contida somente de um mesmo caractere
        repeatCharRule = new RepeatCharacterRegexRule(senha.length());

        List<Rule> ruleList = new ArrayList<Rule>();
        ruleList.add(dictRule);
        ruleList.add(alphabeticalRule);
        ruleList.add(whitespaceRule);
        ruleList.add(qwertySeqRule);
        ruleList.add(alphaSeqRule);
        ruleList.add(repeatCharRule);

        PasswordValidator validator = new PasswordValidator(ruleList);
        PasswordData passwordData = new PasswordData(new Password(senha));
        RuleResult result = validator.validate(passwordData);

        if(!result.isValid()) {
//            for (String msg : validator.getMessages(result)) {
//                System.out.println(msg);
//            }
            return false;
        }
        else {
            return true;
        }
    }

    /* Dicionario carregado na memória pra não ter que toda vez que chamar testarForça
     precisar de novo do arquivo texto */
    private static DictionarySubstringRule dictRule;

    static {
        try {
            // Criacao do dicionario atraves do arquivo de texto que precisa ser no formato UTF-8
            ArrayWordList awl;
            awl = WordLists.createFromReader(
                new FileReader[] {new FileReader(FileUtil.classRealPath(UserValidator.class)+"/res/wordlist_pt_br.txt")},
                true,
                new ArraysSort());
            WordListDictionary dict = new WordListDictionary(awl);
            // Regra do dicionario
            dictRule = new DictionarySubstringRule(dict);
            dictRule.setMatchBackwards(true);// com isso as palavras tambem sao colocadas ao contrario
        }
        catch (Exception e) {
			DefaultExceptionLogger.getInstance().execute(e);
        }
    }

	private void validatePasswordSecurity(User user) {
		String invalidErrorMsg = invalidPasswordMsg(user);
		if(invalidErrorMsg!=null){
			error("password",invalidErrorMsg);
		}
	}
}