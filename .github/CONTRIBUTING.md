# Contributor's Guide

We are happy that you decided to spend your valuable time to help us
make this project better!

We would like you to follow these guidelines:

- [Legal Terms](#legal-terms)
- [Code of Conduct](#code-of-conduct)
- [Question or Problem?](#question-or-problem)
- [Feature Requests / Change Proposals](#features-requests)
- [Issues and Bugs](#issues-and-bugs)
- [Submission Guidelines](#submission-guidelines)

## <a name="legal-terms"></a> Legal Terms

Your contributions are subject to project's
license: [LICENSE](https://github.com/razorapid/morpheus-lang/blob/master/LICENSE.md)

### GitHub Terms of Service

> Whenever you make a contribution to a repository containing notice of a license,
> you license your contribution under the same terms, and you agree that you have
> the right to license your contribution under those terms.
>
> If you have a separate agreement to license your contributions under different terms,
> such as a contributor license agreement, that agreement will supersede.
>
> Isn't this just how it works already?
> Yep. This is widely accepted as the norm in the open-source community;
> it's commonly referred to by the shorthand "inbound=outbound".
> We're just making it explicit.
>
> [GitHub Terms of Service](https://docs.github.com/en/github/site-policy/github-terms-of-service#6-contributions-under-repository-license)

## <a name="code-of-conduct"></a> Code of Conduct

Whenever you make a contribution, you agree with our [Code of Conduct](CODE_OF_CONDUCT.md)

## <a name="question-or-problem"></a> Got a Question or Problem?

Please open an issue with label `question`.

## <a name="features-requests"></a> Got an Idea or Missing a Feature?

You can *request* a new feature by [submitting an issue](#submit-issue) to our [GitHub Repository][github].
If you would like to *implement* a new feature, please submit an issue with
a proposal for your work first, to be sure that we can use it.

First, open an issue and outline your proposal so that it can be discussed.
This will also allow us to better coordinate our efforts, prevent duplication of work,
and help you to craft the change so that it is successfully accepted into the project.

**Note**:
Adding a new topic to the documentation, or significantly re-writing a topic, counts as a
feature.

## <a name="issues-and-bugs"></a> Found a Bug?

If you find a bug in the source code, you can help us by
[submitting an issue](#submit-issue) to our [GitHub Repository][github].
Even better, you can [submit a Pull Request](#submit-pr) with a fix.

Big and substantial bug fixes, that require major effort or re-write should be handled
the same way as feature proposals and have issue submitted and discussed first.

## <a name="submission-guidelines"></a> Submission Guidelines

### <a name="commit"></a> Commit Message Guidelines

- Please follow [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/):
  - types
    - feat - feature
    - fix - bugfix
    - test - implementation of new test cases
    - refactor - code changes that don't change behavior
    - perf - performance improvements
    - build - changes in build scripts
    - ci - changes in CI
    - doc - documentation improvements
    - chore - other work
  - scopes
    - \<language\> - changes for library in given implementation language
    - lexer - changes in the lexer
    - parser - changes in the parser
    - cst - changes in the concrete syntax tree
    - ast - changes in the abstract syntax tree
  - examples
    - feat(java,parser): support arithmetic positive unary operator

Having knowledge on crafting good commit messages is also welcome
(you can read about that in [this article](https://chris.beams.io/posts/git-commit/))

When creating documentation, please try to follow markdown good practice guidelines that can be found
[here](https://www.markdownguide.org)

### <a name="submit-issue"></a> Submitting an Issue

Before you submit an issue, please search the issue tracker, maybe an issue for your problem already exists and the
discussion might inform you of workarounds readily available.

We want to fix all the issues as soon as possible, but before fixing a bug we need to reproduce and confirm it. In order
to reproduce bugs, we will systematically ask you to provide a minimal reproduction. Having a minimal reproducible
scenario gives us a wealth of important information without going back & forth to you with additional questions.

A minimal reproduction allows us to quickly confirm a bug (or point out a coding problem) as well as confirm that we are
fixing the right problem.

We will be insisting on a minimal reproduction scenario in order to save maintainers' time and ultimately be able to fix
more bugs. Interestingly, from our experience, users often find coding problems themselves while preparing a minimal
reproduction. We understand that sometimes it might be hard to extract essential bits of code from a larger codebase but
we really need to isolate the problem before we can fix it.

Unfortunately, we are not able to investigate / fix bugs without a minimal reproduction, so if we don't hear back from
you, we are going to close an issue that doesn't have enough info to be reproduced.

### <a name="submit-pr"></a> Submitting a Pull Request (PR)

Before you submit your Pull Request (PR) consider the following guidelines:

1. Search [GitHub](https://github.com/razorapid/morpheus-lang/pulls) for an open or closed PR
   that relates to your submission. You don't want to duplicate effort.
1. Be sure that an issue describes the problem you're fixing, or documents the design for the feature you'd like to add.
   Discussing the design upfront helps to ensure that we're ready to accept your work.
1. Fork the razorapid/morpheus-lang repo.
1. Make your changes in a new git branch:

     ```shell
     git checkout -b my-fix-branch master
     ```

1. Create your patch.
1. Commit your changes using a descriptive commit message that follows our
   [commit message conventions](#commit).

     ```shell
     git commit -a
     ```
   Note: the optional commit `-a` command line option will automatically "add" and "rm" edited files.

1. Push your branch to GitHub:

    ```shell
    git push origin my-fix-branch
    ```

1. In GitHub, send a pull request to `mohreborn-docs:master`.

- If we suggest changes then:
    - Make the required updates.
    - Rebase your branch and force push to your GitHub repository (this will update your Pull Request):

      ```shell
      git rebase master -i
      git push -f
      ```

That's it! Thank you for your contribution!

#### After your pull request is merged

After your pull request is merged, you can safely delete your branch and pull the changes
from the main (upstream) repository:

- Delete the remote branch on GitHub either through the GitHub web UI or your local shell as follows:

    ```shell
    git push origin --delete my-fix-branch
    ```

- Check out the master branch:

    ```shell
    git checkout master -f
    ```

- Delete the local branch:

    ```shell
    git branch -D my-fix-branch
    ```

- Update your master with the latest upstream version:

   ```shell
   git pull --ff upstream master
   ```

[github]: https://github.com/razorapid/morpheus-lang
